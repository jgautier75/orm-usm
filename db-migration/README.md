java -jar db-migration.jar --classpath=db-migration.jar --driver=org.postgresql.Driver --url="jdbc:postgresql://localhost:5432/orm_usm" --changeLogFile="postgresql/changelogs.xml" --username=tec_orm_usm_dba --password=tec_orm_usm_dba --logLevel=info  update