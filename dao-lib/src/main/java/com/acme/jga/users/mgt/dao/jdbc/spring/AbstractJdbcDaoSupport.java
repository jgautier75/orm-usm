package com.acme.jga.users.mgt.dao.jdbc.spring;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;

import com.acme.jga.search.filtering.expr.Expression;
import com.acme.jga.search.filtering.expr.FilterComparison;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.users.mgt.dto.filtering.FilteringConstants;
import com.acme.jga.users.mgt.dto.pagination.OrderByClause;
import com.acme.jga.users.mgt.dto.pagination.Pagination;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJdbcDaoSupport extends JdbcDaoSupport {
	private static final String DB_DAO_QUERY_FOLDER = "db/sql";
	protected Properties queries = new Properties();
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private static final Integer DEFAULT_PAGE_SIZE = 10;

	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class CompositeQuery {		
		public String query;
		public Map<String,Object> parameters;
		public boolean notEmpty = false;
	}

	protected AbstractJdbcDaoSupport() {
		// Empty constructor for injection
	}

	protected AbstractJdbcDaoSupport(DataSource dataSource, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		setDataSource(dataSource);
	}

	protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	protected void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	/**
	 * Load SQL queries from properties paths using current thread class loader.
	 *
	 * @param queryFilePaths Properties paths
	 */
	protected void loadQueryFilePath(String[] queryFilePaths) {
		if (queryFilePaths != null) {
			String sqlFile = queryFilePaths[0];
			boolean loadSucceeds = true;
			try (InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(DB_DAO_QUERY_FOLDER + "/" + sqlFile)) {
				loadSucceeds = is != null;
			} catch (Exception e) {
				loadSucceeds = false;
			}
			if (loadSucceeds) {
				loadQueryFilePath(Thread.currentThread().getContextClassLoader(), queryFilePaths);
			} else {
				loadQueryFilePath(AbstractJdbcDaoSupport.class.getClassLoader(), queryFilePaths);
			}
		}

	}

	/**
	 * Load SQL queriers from properties paths using the specified class loader.
	 *
	 * @param clazzLoader    Class loader
	 * @param queryFilePaths Properties paths
	 */
	protected void loadQueryFilePath(ClassLoader clazzLoader, String[] queryFilePaths) {
		Arrays.asList(queryFilePaths).forEach(queryFile -> {
			try (InputStream io = clazzLoader.getResourceAsStream(DB_DAO_QUERY_FOLDER + "/" + queryFile)) {
				queries.load(io);
			} catch (IOException e) {
				log.error("loadQueryFilePath [" + Arrays.toString(queryFilePaths) + "]", e);
			}
		});
	}

	/**
	 * Load an SQL instruction from cache.
	 *
	 * @param pKey Query key
	 * @return SQL instruction
	 */
	protected String getQuery(String pKey) {
		return queries.getProperty(pKey);
	}

	/**
	 * Build full sql query from base query, where clause list, order by clause list
	 * and group by clause
	 * 
	 * @param baseQuery         Base SQL query
	 * @param whereClauseList   Where clause list
	 * @param orderByClauseList Order by clause list
	 * @param groupByClause     Group by clause
	 * @return Full SQL query
	 */
	protected String buildFullQuery(String baseQuery, List<WhereClause> whereClauseList,
			List<OrderByClause> orderByClauseList, String... groupByClause) {
		final StringBuilder sb = new StringBuilder(baseQuery);
		int inc = 0;
		if (!whereClauseList.isEmpty()) {
			sb.append(" where ");
			for (final WhereClause whereClause : whereClauseList) {
				if (inc > 0) {
					sb.append(" ").append(whereClause.getOperator().name()).append(" ");
				}
				sb.append("(").append(whereClause.getExpression()).append(")");
				inc++;
			}
		}
		if (groupByClause != null && groupByClause.length > 0) {
			sb.append(" group by ").append(groupByClause[0]);
		}
		if (orderByClauseList != null && !orderByClauseList.isEmpty()) {
			inc = 0;
			for (OrderByClause orderByClause : orderByClauseList) {
				if (inc == 0) {
					sb.append(" order by ");
				} else {
					sb.append(",");
				}
				sb.append(" ").append(orderByClause.getExpression()).append(" ")
						.append(orderByClause.getOrderDirection().name().toUpperCase());
				inc++;
			}
		}
		return sb.toString();
	}

	/**
	 * Build parameters from where clauses list.
	 * 
	 * @param whereClauseList Where clause list
	 * @return Parameters
	 */
	protected Map<String, Object> buildParams(List<WhereClause> whereClauseList) {
		final Map<String, Object> params = new HashMap<>();
		whereClauseList.forEach(whereClause -> {
			if (whereClause.getParamName() != null && whereClause.getParamValue() != null) {
				params.put(whereClause.getParamName(), whereClause.getParamValue());
			}
			if (!CollectionUtils.isEmpty(whereClause.getParamNames())
					&& !CollectionUtils.isEmpty(whereClause.getParamValues())) {
				int inc = 0;
				for (var pName : whereClause.getParamNames()) {
					params.put(pName, whereClause.getParamValues().get(inc));
					inc++;
				}
			}
		});
		return params;
	}

	public String encapsulateCount(String baseQuery) {
		return "select count(1) from (" + baseQuery + ") selData";
	}

	public String paginateQuery(String query, Pagination pagination) {
		if (pagination == null || pagination.getPage() == null || pagination.getPageSize() == null) {
			return query;
		} else {
			int start = (pagination.getPage() - 1) * pagination.getPageSize();
			return query + " limit " + pagination.getPageSize() + " offset " + start;
		}
	}

	public boolean executeExists(String query, Map<String, Object> params) {
		Integer nbResults = getNamedParameterJdbcTemplate().query(query, params, rs -> {
			Integer nbResults1 = null;
			if (rs.next()) {
				nbResults1 = rs.getInt(1);
			}
			return nbResults1;
		});
		return nbResults != null && nbResults > 0;
	}

	/**
	 * Extract id generated from sequence.
	 * 
	 * @param keyHolder   KeyHolder
	 * @param targetField Target column name (usually 'id')
	 * @return Generated id
	 */
	public Long extractGeneratedId(KeyHolder keyHolder, String targetField) {
		if (keyHolder != null && keyHolder.getKeys() != null && keyHolder.getKeys().get(targetField) != null) {
			return Long.valueOf(keyHolder.getKeys().get(targetField).toString());
		} else {
			return null;
		}
	}

	/**
	 * Build postgreSQL object from json value.
	 * 
	 * @param json Json value
	 * @return PGobject
	 * @throws SQLException SQL exception
	 */
	public PGobject buildPGobject(String json) throws SQLException {
		PGobject jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(json);
		return jsonObject;
	}


	public CompositeQuery buildQuery(Map<String,Object> searchParams){		
		ParsingResult parsingResult = (ParsingResult) searchParams.get(FilteringConstants.PARSING_RESULTS);
		StringBuilder sb = new StringBuilder();
		Map<String,Object> params = new HashMap<>();
		int inc = 0;
		StringBuilder paramName = new StringBuilder();
		boolean isEmpty = true;
		if (parsingResult!=null && !CollectionUtils.isEmpty(parsingResult.getExpressions())){
			isEmpty = false;
			for (Expression expression : parsingResult.getExpressions()){
				switch (expression.getType()) {
					case OPENING_PARENTHESIS:
						sb.append(" ( ");
						break;
					case COMPARISON:
						 sb.append(buildComparison(expression.getValue()));
						break;
					case CLOSING_PARENTEHSIS:
						sb.append(" ) ");
						break;
					case NEGATION:
						sb.append(" not ");
						break;
					case OPERATOR:
						sb.append(buildOperator(expression.getValue()));
						break;
					case PROPERTY:
						sb.append(stripEnclosingQuotes(expression.getValue()));
						paramName.setLength(0);
						paramName.append("p"+stripEnclosingQuotes(expression.getValue())).append(inc);
						break;					
					case VALUE:
						sb.append(":"+paramName.toString());						
						params.put(paramName.toString(),stripEnclosingQuotes(expression.getValue()));
						break;					
					default:
						break;
				}
				inc++;
			}

			Integer pageIndex = (Integer) searchParams.get(FilteringConstants.PAGE_INDEX);
			if (pageIndex==null || pageIndex==0) {
				pageIndex = 1;
			}
			Integer pageSize = (Integer) searchParams.get(FilteringConstants.PAGE_SIZE);
			if (pageSize==null){
				pageSize = DEFAULT_PAGE_SIZE;
			}
			int start = (pageIndex - 1) * pageSize;
			sb.append(" limit " + pageSize + " offset " + start);

		}
		return new CompositeQuery(sb.toString(),params,!isEmpty);
	}

	protected String stripEnclosingQuotes(String value){
		if (value!=null && value.startsWith("'") && value.endsWith("'")){
			String leftPart = value.substring(1);
			return leftPart.substring(0, leftPart.length()-1);
		}else {
			return value;
		}
	}

	protected String buildOperator(String operator){
		if (operator!=null && "AND".equalsIgnoreCase(operator)){
			return " and ";
		}else if (operator!=null && "OR".equalsIgnoreCase(operator)){
			return " or ";
		}else {
			return "";
		}
	}

	protected String buildComparison(String comparison){
		if (comparison!=null && FilterComparison.EEQUALS.equalsIgnoreCase(comparison)){
			return "=";
		}else if (comparison!=null && FilterComparison.GREATER_OR_EQUALS.equalsIgnoreCase(comparison)){
			return ">=";
		}else if (comparison!=null && FilterComparison.GREATER_THAN.equalsIgnoreCase(comparison)){
			return ">";
		}else if (comparison!=null && FilterComparison.LIKE.equalsIgnoreCase(comparison)){
			return "like";
		}else if (comparison!=null && FilterComparison.LOWER_OR_EQUALS.equalsIgnoreCase(comparison)){
			return "<=";
		}else if (comparison!=null && FilterComparison.LOWER_THAN.equalsIgnoreCase(comparison)){
			return "<";
		}else if (comparison!=null && FilterComparison.NOT_EQUALS.equalsIgnoreCase(comparison)){
			return "!=";
		}else {
			return "";
		}

	}

}
