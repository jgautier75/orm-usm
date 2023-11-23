create database "orm_usm" with encoding 'UTF-8' connection limit -1;

CREATE USER tec_orm_usm_dba WITH PASSWORD 'tec_orm_usm_dba';
GRANT ALL PRIVILEGES ON DATABASE "orm_usm" TO tec_orm_usm_dba;
ALTER ROLE tec_orm_usm_dba NOSUPERUSER NOCREATEDB CREATEROLE INHERIT LOGIN;

create user tec_orm_usm_app with PASSWORD 'tec_orm_usm_app';
ALTER ROLE tec_orm_usm_app NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;

grant all privileges on schema public to tec_orm_usm_dba;
