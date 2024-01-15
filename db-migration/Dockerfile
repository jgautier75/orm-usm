FROM alpine:3.19.0
ARG JAR_FILE
ARG INITSH
ARG DBSQLFILE
ARG ACTSQLFILE

RUN apk add --no-cache postgresql-client openjdk21

COPY ${JAR_FILE} db-migration.jar
COPY ${INITSH} init.sh
COPY ${DBSQLFILE} create_database.sql
COPY ${ACTSQLFILE} accounts_setup.sql

CMD ["./init.sh"]