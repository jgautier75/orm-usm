#!/bin/sh

echo "P_PGHOST=$P_PGHOST"
echo "P_PGPORT=$P_PGPORT"
echo "P_PGUSER=$P_PGUSER"
echo "P_PGPASS=$P_PGPASS"
echo "P_DBNAME=$P_DBNAME"
echo "P_DBAUSER=$P_DBAUSER"
echo "P_DBAPASS=$P_DBAPASS"
echo "P_APPUSER=$P_APPUSER"
echo "P_APPPASS=$P_APPPASS"
DEFAULT_DB="postgres"

#./init.sh 192.168.1.15 5432 postgres posgres orm-test dba_user dba_pass app_user app_pass

export PGPASSWORD=${P_PGPASS}

DB_EXISTS=$(psql -h $P_PGHOST -U $P_PGUSER -d $DEFAULT_DB -bc "SELECT 1 FROM pg_database WHERE datname='$P_DBNAME'")

TEST_EXISTS="1 row"
CLEANED=${DB_EXISTS//[$'\t\r\n']} && CLEANED=${CLEANED%%*( )}
EXISTS=$(echo "$CLEANED" | grep "$TEST_EXISTS" | wc -l)
if [ "$EXISTS" == "1" ]; then
    echo "Database [$P_DBNAME] exists -> OK"
else
    echo "Database [$P_DBNAME] does not exist"
    sed -e "s/DBNAME/${P_DBNAME}/g" create_database.sql > db_create.sql
    psql postgresql://${P_PGUSER}:${P_PGPASS}@${P_PGHOST}:${P_PGPORT} -f db_create.sql
fi

USR_DBA_EXISTS=$(psql -h $P_PGHOST -U $P_PGUSER -d $DEFAULT_DB -bc "select 1 from pg_roles where rolname = '$P_DBAUSER'")
CLEANED=${USR_DBA_EXISTS//[$'\t\r\n']} && CLEANED=${CLEANED%%*( )}
EXISTS=$(echo "$CLEANED" | grep "$TEST_EXISTS" | wc -l)
if [ "$EXISTS" == "1" ]; then
    echo "User account [$P_DBAUSER] exists -> OK"
else
    echo "User account [$P_DBAUSER] does not exist"
    sed -e "s/DBNAME/${P_DBNAME}/g" -e "s/DBAUSER/${P_DBAUSER}/g" -e "s/DBAPASS/${P_DBAPASS}/g" -e "s/APPUSER/${P_APPUSER}/g" -e "s/APPPASS/${P_APPPASS}/g" accounts_setup.sql > accounts.sql 
    psql postgresql://${P_PGUSER}:${P_PGPASS}@${P_PGHOST}:${P_PGPORT} -f accounts.sql
fi

java -jar db-migration.jar --classpath=db-migration.jar --driver=org.postgresql.Driver --url="jdbc:postgresql://${P_PGHOST}:${P_PGPORT}/${P_DBNAME}" --changeLogFile="postgresql/changelogs.xml" --username="${P_DBAUSER}" --password="${P_DBAPASS}" --logLevel=info  update
