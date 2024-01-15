FROM alpine:3.19.0
ARG JAR_FILE
ARG INITSH
ARG DBSQLFILE
ARG ACTSQLFILE
ARG LIQUITEMP
ARG GRANTSTEMP
ARG GRANTSDBA

RUN apk add --no-cache postgresql-client openjdk21

COPY ${JAR_FILE} db-migration.jar
COPY ${INITSH} init.sh
COPY ${DBSQLFILE} create_database.sql
COPY ${ACTSQLFILE} accounts_setup.sql
COPY ${LIQUITEMP} liquibase_template.properties
COPY ${GRANTSTEMP} grants_template.sql
COPY ${GRANTSDBA} grants_dba_template.sql

CMD ["./init.sh"]