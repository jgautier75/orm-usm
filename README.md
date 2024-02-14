# orm-usm

Organizations &amp; Users Management

[Multi-tenant](https://en.wikipedia.org/wiki/Multitenancy) designed spring-boot application for users management.

A multi-tenants software architecture aims at serving multiple organizations.

Code is organized in a "domain driven" way where:

- "ports" interfaces REST apis and Domain business layer, basically converting from REST DTO format to
  domain entities format.
- "domain" layer contains all business rules and is agnostic either from REST format and to underlying storage layer
- "infrastucture" layer ensures format conversion between domain and the persistence layer

## Technical stack

Standard REST application relying on:

- Sprint boot (3.2.x)
- PostgreSQL (16.x) for persistence
- Liquibase for rdbms schema versions management
- Spring JDBC for persistence (No f**\*\*** ORM)
- Spring integration for PublishSubscribe channel
- OpenTelemetry + Jaeger
- Kafka stack (Kafka + Zookeeper + Schema registry + AKHQ)
- Testcontainers and Mockito for unit testing

## Docker

Docker compose file: docker/docker-services.yml

`docker-compose -f docker/docker-services.yml up -d`

Services:

| Service                         | Version | Port               |
| --------------------------------| ------- | ------------------ |
| postgreSQL                      | 16.1    |  5432              |
| akhq                            | 0.24.0  |  8086              |
| zookeeper                       | 7.4.3   |  2181              |
| kafka                           | 7.4.3   |  9092              |
| schema-registry                 | 7.4.3   |  8085              |
| jaeger-all-in-one               | 7.4.3   | 16686              |
| prometheus                      | v2.49.1 |  9090              |
| grafana                         | 9.5.15  |  3000              |
| opentelemetry-collector-contrib | 0.92.0  |  4317, 4318, 55679 |

### Database setup


#### Manually

Database schema management relies on liquibase, to setup:

1. Connect to postgreSQL container and execute db_accounts_setup.sql which:

- Creates database
- Creates accounts:
  - tec_orm_usm_dba: database account with DDL authorizations (Data Definition Language)
  - tec_orm_usm_app: database account with only DML authorizations (Data Modeling Language). This account is used by spring boot application since applicative accounts must not have thr rights to alter database schema

2. Package project

```java
mvn clean package install -DskipTests
```

3. Move to db-migration/target folder

4. Perform Liquibase update manually:

```java
java -jar db-migration.jar --classpath=db-migration.jar --driver=org.postgresql.Driver --url="jdbc:postgresql://localhost:5432/orm_usm" --changeLogFile="postgresql/changelogs.xml" --username=tec_orm_usm_dba --password=tec_orm_usm_dba --logLevel=info --contexts="all,grants" update
```

#### Docker image

Building the database init docker image:

```sh
docker build . -t db-migration:1.2.0 --build-arg="JAR_FILE=target/db-migration.jar" --build-arg="INITSH=scripts/init.sh" --build-arg="ACTSQLFILE=sql/accounts_setup.sql" --build-arg="DBSQLFILE=sql/create_database.sql" --build-arg="LIQUITEMP=liquibase/liquibase_template.properties" --build-arg="GRANTSTEMP=sql/grants_template.sql" --build-arg="GRANTSDBA=sql/grants_dba_template.sql"
```

```sh
docker run -it --env P_PGHOST=192.168.1.15 --env P_PGPORT=5432 --env P_PGUSER=postgres --env P_PGPASS=posgres --env P_DBNAME=orm-test --env P_DBAUSER=orm_dba --env P_DBAPASS=dba_pass --env P_APPUSER=orm_app --env P_APPPASS=pass_app ec29fd57abd9
```

## Entities

- **Tenant**:
  - A tenant aims at serving multiple organizations.
  - Properties:
    - id: internal identifier
    - uid: external identifier (UUID)
    - code: functional code (unique)
    - label: tenant's label
- **Organization**:
  - An organization belongs to a tenant and holds users
  - Properties:
    - id: internal identifier
    - uid: external identifier (UUID)
    - tenant: Refererce on tenant
    - label: Organization's label
    - code: functional code (unique)
    - kind: Organization's code (Enumeration: TENANT,BU,COMMUNITY,ENTERPRISE)
    - country: Country code (ISO 3166-1 Alpha2)
    - status: status (Enumeration: DRAFT, ACTIVE, INACTIVE)
- **User**:
  - A user belongs to an organization and thus to a tenant
  - Properties:
    - id: internal identifier
    - uid: external identifier (UUID)
    - tenant_id: Reference on tenant
    - org_id: Reference on organization
    - login: User login (unique)
    - firstName: First name
    - lastName: Last name
    - middleName: Middle name
    - email: Email address
    - status: (Enumeration: DRAFT, ACTIVE, INACTIVE)
    - notif_email: Email for notifications (non unique, e.g: a diffusion list)
- **Events**:
  - Storage of audit events.
  - An audit event is always recorded when an entity is created (tenant, organization, sector), updated or deleted
  - Properties:
    - uid: A uique identifier (uuid)
    - created_at: Creation timestamp (UTC/ISO-8601)
    - last_updated_at: Last update timestamp (UTC/ISO-8601)
    - target: Entity type (Enumeration: Tenant(0), Organization(1), User(2), Sector(3))
    - object_uid: Entity object uid
    - action: Enumeration: CREATE, UPDATE, DELETE
    - status: Event status (Enumeration: PENDING(0), PROCESSED(1), FAILED(2))
    - payload: Audit event in json format (PostgreSQL jsonb)

## OpenAPI

REST endpoints OpenApi specifications: docs/orm_usm_openapi.yml

## Audit events

Everytime an entity (tenant, organization, user, sector) is created, updated or deleted, an audit event is persisted in rdbms.

**Why persisting audit events in rdbms and not sending event directly to kafka ?**

When talking about audit events, we must ensure events and related data in rdbms are **consistent**.

Indeed, we want to avoid the following two use cases:

- A rollback is performed in rdbms but the event is still sent and thus the audit event does not reflect the underlying data.
- For some reasons, the kafka brokers are not reacheable (network failure for example) and we try to push directlty in a kafka topic. In this case, either transation is rollback if message sending is within the same transactional method or message is not sent at all to kafka if outside transactional method.

Thus, to ensure consistency between data stored in rdbms and audit event, these ones are stored in rdbms in the same transaction than the data. Obvisouly, we're here relying on ACID features of potgreSQL relational database.
In other words, if a transaction rollback occurs, both data and audit events are rollbacked.

![](docs/images/AuditEvents.drawio.png)

Schema above describes this behaviour, the audit event is created in the same transaction than data.
Once audit events and data have been persisted, a "wakeup" message is sent to a spring-integration PublishSubscribe channel

```java
eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
```

Listener "eventAuditChannel" responsibilities are:

- Retrieve "pending" events from rdbms
- Push events to kafka
- Mark events are processed

![](docs/images/AuditEventListener.drawio.png)

## Protobuf

Messages sent to kafka are likely to change across time (new fields, refactoring, ...). Furthermore, you cannot expect all consumers to migrate to the new version at the same time. As a consequence, messages versioning must be handled.

Google [Protobuf](https://protobuf.dev) provides a convenient way to manage versioning and also offers better performance than standard json or other binary formats.

Protobuf relies on message definitions in [.protoc](https://protobuf.dev/programming-guides/proto3/) format (see domain/src/protobuf/event.proto).

To generate java pojos from .proto file, execute maven command:

```sh
mvn clean generate-sources
```

NB: java pojos are generated in target/generated-sources (check protoc-jar-maven-plugin configuration)

Audit events are sent to kafka using protobuf format (see EventBusHandler class).

In case multiple versions of messages are available (e.g: a v1 and a v2), how to we know at consumer side which version to use ?

This is where schema-registry comes into play.

Basically, schema-registry stores in a versioned way messages definitions.

To list schemas-registry stored schemas, just use a simple [REST api](https://docs.confluent.io/platform/current/schema-registry/develop/api.html):

```sh
curl -L http://localhost:8085/schemas
```

Response:

```json
[
	{
		"subject": "audit_events-value",
		"version": 1,
		"id": 1,
		"schemaType": "PROTOBUF",
		"schema": "syntax = \"proto3\";\npackage com.acme.users.mgt.events.protobuf;\n\nmessage AuditEventMessage {\n  string createdAt = 1;\n  string lastUpdatedAt = 2;\n  string uid = 3;\n  int32 target = 4;\n  .com.acme.users.mgt.events.protobuf.AuditAuthor author = 5;\n  .com.acme.users.mgt.events.protobuf.AuditScope scope = 6;\n  string objectUid = 7;\n  string action = 8;\n  int32 status = 9;\n  repeated .com.acme.users.mgt.events.protobuf.AuditChange changes = 10;\n}\nmessage AuditAuthor {\n  string uid = 1;\n  string name = 2;\n}\nmessage AuditScope {\n  string tenantUid = 1;\n  string tenantName = 2;\n  string organizationUid = 3;\n  string organizationName = 4;\n}\nmessage AuditChange {\n  string object = 1;\n  string from = 2;\n  string to = 3;\n}\n"
	}
]
```

Obviously the schema above is the content of the .proto file define earlier in this document.

In this spring-boot prototype, spring send automatically schemas to registry.

Consumer configuration points to schema registry:

```java
@Bean
Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "generic-protobuf-consumer-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
    props.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
    return props;
}
```

Listener itself:
```java
@Service
@RequiredArgsConstructor
public class KafkaSimpleConsumer {
    private final ILogService logService;

    @KafkaListener(topics = "${app.kafka.producer.topicNameAuditEvents}", groupId = "${app.kafka.consumer.auditEventsGroupId}")
    public void consume(ConsumerRecord<String, AuditEventMessage> messageRecord) {
        logService.infoS(this.getClass().getName(), "Received message: [%s]", new Object[] { messageRecord.value() });
    }

}
```

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

## Technical errors:

When a technical error occurs, the controller advice generates a technical report file and a micrometer gauge is incremented (see MicrometerPrometheus class).

Moreover, the gauge value is exported in actuator/prometheus endpoint and thus can be scrapped by a prometheus job (tech_errors is example below).

![](docs/images/micrometer-prometheus-gauge.png)

## Minimizing JDK in Docker image for spring-boot application

In order to minimize java runtime in webapi Docker image, jdeps tool must be used to determine which java jdk modules are used by application.

To achieve this, first compile package maven project.

Once project bas been compiled, run scripts/get-spring-boot-modules.sh:

Parameters:

- 1: Full path to spring-boot fat jar
- 2: Jdk version (17)
- 3: Temp directory for spring-boot app extraction
- 4: Automatic modules: list of automatic modules, typically legacy libraries (multiple values separator is the comma)

```sh
./get-springboot-modules.sh webapi/target/webapi.jar 17 webapi/target/tmp "snakeyaml-1.28.jar,jakarta.annotation-api-1.3.5.jar,slf4j-api-1.7.32.jar"
```

Update webapi/Dockerfile accordingly in jlinks section

`RUN jlink --compress=2 --no-header-files --no-man-pages --add-modules java.base,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql.rowset,jdk.compiler,jdk.jfr,jdk.management,jdk.unsupported,jdk.crypto.ec  --output /app/customjre`

Building Docker image:

```sh
docker build . -t orm-usm-webapi:1.0.0 --build-arg="JAR_FILE=target/webapi-1.0.0-SNAPSHOT.jar"
```

## Native image with GraalVM

Prerequisite: Graalvm installed https://www.graalvm.org/downloads/

To build a native image run the following command to build all maven modules:

```sh
mvn clean package -DskipTests
```

Then move to webapi directory and run:

```sh
mvn clean package -DskipTests -Pnative
```

Command above relies on https://graalvm.github.io/native-build-tools/latest/maven-plugin.html

Plugin configuration example in webapi/pom.xml file

```xml
<profile>
      <id>native</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
            <version>0.9.28</version>
            <executions>
              <execution>
                <id>build-native</id>
                <goals>
                  <goal>compile-no-fork</goal>
                </goals>
                <configuration>
                  <buildArgs>
                    <arg>-H:+UnlockExperimentalVMOptions</arg>
                    <arg>-H:IncludeResources=.*properties$</arg>
                    <arg>-H:ReflectionConfigurationFiles=../spring-native/reflect-config.json</arg>
                  </buildArgs>
                </configuration>
                <phase>package</phase>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
```

**TIPS**

Since AOT(**A**head **O**f **T**ime) is a kind of static compilation, reflection mechanisms cannot be handled the same way than JIT (**Just** **In** **T**ime) which occurs at runtime.

Thus, to use reflection like jackson when serializing / deserializing from/to json, description of fields and methods of DTOs (**Data** **T**ransfer **Object**) might be required.

To achieve this, a reflect-config file must be designed to indicate how to seriaize / deserialize from a DTO / JSON.

See https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/

In this project, spring-native/reflect-config.json file describes classes like:

- AuditEvent: These objects are serialized in json before being persisted in rdbms
- AuditScope: A nested object of AuditEvent
- ApiError: Standard POJO returned as json when an error occurs in REST controllers

**Youtube Spring I/O**:

- https://www.youtube.com/watch?v=8umoZWj6UcU
- https://www.youtube.com/watch?v=HWUy0kTlcj8

## Keycloak SPI API

Activity diagram for authentication with Keycloak.

![](docs/images/UserAuth.png)

SPI API on Keycloak side is a jar with a class implementing at least:

- org.keycloak.storage.UserStorageProvider;
- org.keycloak.storage.user.UserLookupProvider;

In this jar, declare a file in /src/main/resources/META-INF/services/org.keycloak.storage.UserStorageProviderFactory containing implementation class.

## Minikube:

Enabling ingress (nginx):

```sh
minikube addons enable ingress
```

Verifying nginx ingress is up:

```sh
kubectl -n ingress-nginx get pods
```

Using minikube docker

```sh
minikube docker-env
```

```sh
eval $(minikube -p minikube docker-env)
```

Listing images:

```sh
minikube image ls --format table
```

### Init Database

Please refer to Docker > Database setup to build image.

In kube/orm-usm-bd-init/values.yaml, ensure parameters are the right ones.

Note: db-migration scripts are idempotent, so if database and/or accounts already exist, they are not re-created.

When image db-migration:x.y.z is ready, run the folling helm command in kube directory:
```sh
helm template orm-usm-bd-init orm-usm-bd-init | kubectl apply -f -
```

Command above will deploy a pod in kubernetes, this pod performs liquibase update.

### WebApi

Either save image and load in minikube:

```sh
docker save 4ed63bb1fddb --output orm-usm-webapi.tar
minikube image load orm-usm-webapi.tar
```

Or build image in minikube:

```sh
minikube image build . -t orm-usm-webapi:1.0.0 --build-env="JAR_FILE=target/webapi-1.0.0-SNAPSHOT.jar"
```

Templating with helm and deploying:

```sh
helm template orm-usm-webapi orm-usm-webapi | kubectl apply -f -
```

Opening service endpoint

```sh
minikube service orm-usm-webapi
```

## OpenTelemetry

Goal of [OpenTelemetry](https://opentelemetry.io/) is used to instrument, generate, collect, and export telemetry data (metrics, logs, and traces) to help you analyze your software’s performance and behavior.

OpenTelemetry collector config: otel-collector-config.yml

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: otel-collector:4317
  otlp/2:
    protocols:
      grpc:
        endpoint: otel-collector:55679

exporters:
  otlp/jaeger: # Jaeger supports OTLP directly. The default port for OTLP/gRPC is 4317
    endpoint: http://jaeger:4317
    tls:
      insecure: true

processors:
  batch:

extensions:
  health_check:

service:
  extensions: [health_check]
  pipelines:
    traces:
      receivers: [otlp,otlp/2]
      processors: [batch]
      exporters: [otlp/jaeger]
```

No additional dependency required in maven project.

Launch application with the following arguments:

```sh
java -javaagent:/home/jgautier/git-data/opentelemetry-javaagent.jar -Dotel.service.name=orm-usm-webapi -Dotel.traces.exporter=jaeger -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.javaagent.debug=true -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=none -jar webapi-1.0.0-SNAPSHOT.jar
```

Update -Dotel.javaagent.debug=true argument to disable opentelementry java agent debug mode.

OpenTelemetry spans logs:

```sh
otel.javaagent 2024-01-17 22:36:06:457 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT orm_usm.tenants' : 62dd1652c7fe3942a5c33403e84f0f44 00c752df75943a17 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=tenants, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select id,uid,code,label from tenants where (uid=?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:460 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT orm_usm.organizations' : 62dd1652c7fe3942a5c33403e84f0f44 edb8737ec0cc3856 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=organizations, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select id from organizations where code=?, server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:463 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT orm_usm.organizations' : 62dd1652c7fe3942a5c33403e84f0f44 ca323db9cd220da5 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=organizations, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into organizations(tenant_id,uid,code,label,kind,country,status) values (?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:469 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT orm_usm.events' : 62dd1652c7fe3942a5c33403e84f0f44 fb12855a3d92f909 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=events, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into events(uid,created_at,last_updated_at,target,object_uid,action,status,payload) values(?,?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:470 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT orm_usm.sectors' : 62dd1652c7fe3942a5c33403e84f0f44 8021aa0286c870ad CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=sectors, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into sectors(uid,tenant_id,org_id,label,code,root,parent_id) values(?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
2024-01-17 22:36:06,471 INFO  [http-nio-8080-exec-1] - com.acme.users.mgt.infra.services.impl.sectors.SectorsInfraServicecreateSector: Created sector with uid [8939cdfb-7543-46bb-8626-a03d3cf345f2] on tenant [1] and organization [5]
[otel.javaagent 2024-01-17 22:36:06:472 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT orm_usm.events' : 62dd1652c7fe3942a5c33403e84f0f44 f50362fdb0ee9610 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=events, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into events(uid,created_at,last_updated_at,target,object_uid,action,status,payload) values(?,?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
2024-01-17 22:36:06,483 DEBUG [http-nio-8080-exec-1] - com.acme.users.mgt.events.EventBusHandler: Handling wakeup message
[otel.javaagent 2024-01-17 22:36:06:485 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT orm_usm.events' : 62dd1652c7fe3942a5c33403e84f0f44 7d3459dbd94ab94c CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=events, db.name=orm_usm, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=tec_orm_usm_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select uid,created_at,last_updated_at,target,object_uid,action,status,payload from events where (status=?) order by created_at ASC, server.port=5432}, capacity=128, totalAddedValues=11}
```


Organization creation in Jaeger UI:

![](docs/images/otel_create_orga.png)