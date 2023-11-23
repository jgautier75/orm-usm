# orm-usm
Organizations &amp; Users Management

[Multi-tenant](https://en.wikipedia.org/wiki/Multitenancy) designed spring-boot application for users management.

A multi-tenants software architecture aims at serving multiple organizations.

## Technical stack

Standard REST application relying on:

* PostgreSQL (16.x) for persistence
* Liquibase for rdbms schema versions management
* Spring boot JDBC for persistence (No f****** ORM)
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

![Insomnia](docs/insomnina.png)