# orm-usm
Organizations &amp; Users Management

[Multi-tenant](https://en.wikipedia.org/wiki/Multitenancy) designed spring-boot application for users management.

A multi-tenants software architecture aims at serving multiple organizations.

## Technical stack

Standard REST application relying on:

* PostgreSQL (16.x) for persistence
* Liquibase for rdbms schema versions management
* Spring boot JDBC for persistence (No f****** ORM)
* HikariCP for connection pooling 
* Sprint boot (3.1.x)
* Testcontainers and Mockito for unit testing (docker containers like postgreSQL)

## Entities

* **Tenant**: 
    * A tenant aims at serving multiple organizations.
    * Properties:
        * id: internal identifier
        * uid: external identifier (UUID)
        * code: functional code (unique)
        * label: tenant's label
* **Organization**:
    * An organization belongs to a tenant and holds users
    * Properties:
        * id: internal identifier
        * uid: external identifier (UUID)
        * tenant: Refererce on tenant
        * label: Organization's label
        * code: functional code (unique)
        * kind: Organization's code (Enumeration: TENANT,BU,COMMUNITY,ENTERPRISE)
        * country: Country code (ISO 3166-1 Alpha2)
        * status: status (Enumeration: DRAFT, ACTIVE, INACTIVE)
* **User**:
    * A user belongs to an organization and thus to a tenant
    * Properties:
        * id: internal identifier
        * uid: external identifier (UUID)
        * tenant_id: Reference on tenant
        * org_id: Reference on organization
        * login: User login (unique)
        * firstName: First name
        * lastName: Last name
        * middleName: Middle name
        * email: Email address
        * status: (Enumeration: DRAFT, ACTIVE, INACTIVE)

## Testing REST APIS

An [Insomnia](https://insomnia.rest/) collection is available in docs directory.

## Debugging HTTP requests

A particular HTTP filter (see: LogHttpFilter class in webapi module) has been designed to be able to debug a single HTTP request.

Indeed, the problem with changing level mode of loggers is that all incoming traffic is debugged.

To avoid this, since it's a classic REST API, one HTTP request => One thread.

Thus, a ThreadLocal (LogHttpUtils.APP_LOG_CTX) can be used to position a debug flag depending on the presence or absence of an HTTP header in request.

This custom header is customizable in application.yml (Defaults to X-APP-DEBUG)

Moreover, the unique Spring LogService logs requests depending of the ThreadLocal value set by filter.

Finally, by default all requests are debugged, this behaviour is controlled by "forceDebugMode" parameter in application.yml

## Custom JDK

In order to minimize java runtime in webapi Docker image, jdeps tool must be used to determine which java jdk modules are used by application.

To achieve this, first compile package maven project.

Once project bas been compiled, run scripts/get-spring-boot-modules.sh:

Parameters:
* 1: Full path to spring-boot fat jar
* 2: Jdk version (17)
* 3: Temp directory for spring-boot app extraction
*   4: Automatic modules: list of automatic modules, typically legacy libraries (multiple values separator is the comma)

Example: get-springboot-modules.sh webapi/target/webapi.jar 17 webapi/target/tmp "snakeyaml-1.28.jar,jakarta.annotation-api-1.3.5.jar,slf4j-api-1.7.32.jar"

Update webapi/Dockerfile accordingly in jlinks section

`RUN jlink --compress=2 --no-header-files --no-man-pages --add-modules java.base,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql.rowset,jdk.compiler,jdk.jfr,jdk.management,jdk.unsupported,jdk.crypto.ec  --output /app/customjre`