## Syndesis Integration Tests

This repository contains integration tests for Syndesis. The tests start integration runtimes in 
[Docker](https://www.docker.com/) (using [Testcontainers](https://www.testcontainers.org/)) and exchange messages with
the running integration. The integration outcome gets consumed and validated by simulated 3rd party services and/or within 
the database.

##### Table of Contents

* [Setup and preparations](#setup-and-preparations)
* [Test environment](#test-environment)
* [Running tests](#running-tests)
    * [Use latest snapshot versions](#use-latest-snapshot-versions)
    * [Use release versions](#use-release-versions)
* [Syndesis integration runtime container](#syndesis-integration-runtime-container)
  * [From integration export](#from-integration-export)
  * [From integration model](#from-integration-model)
  * [From integration fat jar](#from-integration-fat-jar)
  * [From integration project](#from-integration-project)
  * [From integration json](#from-integration-json)
* [Syndesis db container](#syndesis-db-container)
* [Syndesis server container](#syndesis-server-container)
* [Infrastructure containers](#infrastructure-containers)
  * [AMQ Message Broker](#amq-message-broker)
  * [Kafka Message Broker](#kafka-message-broker)
* [Simulate 3rd party interfaces](#simulate-3rd-party-interfaces)
* [Customize integrations](#customize-integrations)
* [Logging](#logging)
* [Debugging](#debugging)

## Setup and preparations

The integration tests in this repository use [JUnit](https://junit.org/), [Testcontainers](https://www.testcontainers.org/) 
and [Citrus](https://citrusframework.org/) as base frameworks.

The frameworks are automatically downloaded for you using Maven so you do not worry about having to download or install these tools. 

Each integration test prepares and starts a group of Docker images as Testcontainers. Therefore you need [Docker](https://www.docker.com/) 
available on your host to run the tests.
 
The containers used in the integration test project represent Syndesis backend servers, Syndesis integration runtimes and infrastructure components 
such as Postgres DB, Kafka or AMQ message brokers. In addition to that the tests prepares simulated 3rd party services with the [Citrus](https://citrusframework.org/) framework.

## Test environment

The integration tests usually exchange data with Syndesis integrations. Therefore the Syndesis integration runtime is the 
primary system under test.

Each test uses a specific group of required infrastructure components and builds its very specific Syndesis integration runtime 
as a Testcontainer. The test builds and runs all required infrastructure components and the defined Syndesis integration runtime automatically. If not 
already available on your local host the integration tests will automatically load all artifacts (such as Docker images, Java libraries and Syndesis artifacts)
using an internet connection.  

Once the test infrastructure is setup with Testcontainers the test interacts with a running Syndesis integration. Usually the test invokes/triggers
the integration starting connection and consumes the integration output for verification.

Each test defines the required components individually so required Testcontainers are automatically started and stopped before and after the tests. All tests share
a common Postgres database container that holds the Syndesis persistent data as well as sample database tables (todo and contact) used with some test data.

### System properties / environment variables

You can influence the test environment by setting several properties or environment variables for the test runtime. You can set these 
as system properties in Maven and/or in your Java IDE or as environment variables on your host.

The following system properties (or environment variables) are known to the project

* **syndesis.version** / **SYNDESIS_VERSION**
    * Version of Syndesis used as system under test. By default this is the latest SNAPSHOT version. You can also use tagged 
    release or daily build versions as listed here: [https://github.com/syndesisio/syndesis/releases](https://github.com/syndesisio/syndesis/releases)
    Maven artifact versions are translated to Docker hub image versions. For example the Maven snapshot version `1.7-SNAPSHOT` is 
    translated to the Docker image tag `latest`. Specifying a Syndesis release version is very useful to run the tests with a specific release or nightly build.
* **syndesis.image.tag** / **SYNDESIS_IMAGE_TAG**
    * Docker image tag to use for all Syndesis images. You can use this explicit image version when automatic version translation 
    form Maven artifact name is not working for you.
* **syndesis.debug.port** / **SYNDESIS_DEBUG_PORT**
    * Set the debug port to use for remote debugging sessions (default=5005).
* **syndesis.debug.enabled** / **SYNDESIS_DEBUG_ENABLED**
    * Enables remote debug on all integration runtime and syndesis-server containers (default=false).
* **syndesis.logging.enabled** / **SYNDESIS_LOGGING_ENABLED**
    * GLobally enables container logging (default=false).
* **syndesis.s2i.build.enabled** / **SYNDESIS_S2I_BUILD_ENABLED**
    * By default the test containers use a Spring Boot build and runtime environment in the Syndesis integration runtime container. You can also use
    the S2i image to build and run the integration. The S2i image build is very close to production but indeed slower in its build time.

## Running tests

You can run the tests from your favorite Java IDE (e.g. Eclipse, IntelliJ) as normal JUnit test. Also you can run all available tests with
Maven build tool:

```bash
mvn verify
```

This will execute all available integration tests. You can also run single tests or test methods. Just give the test class name and/or test method name as
an argument.

```bash
mvn verify -Dit.test=MyTestClassName
```

```bash
mvn verify -Dit.test=MyTestClassName#mytestMethodName
```

## Use latest snapshot versions

If you do not specify anything different the integration tests will use the latest Syndesis version available. This can be the Syndesis Docker images tagged with `latest` or
a local build of Syndesis using the very latest code base on your local host.

In case you want to use the very latest code base on your local host you need to clone and build the Syndesis project first on your machine.

```bash
git clone https://github.com/syndesisio/syndesis.git
```

After cloning the project you have to build the whole thing with:

```bash
cd syndesis
tools/bin/syndesis build -f
```

Now you can build the Docker images locally:

```bash
tools/bin/syndesis build -m s2i -i -f --docker
```

After that you should see a new Docker image `syndesis/syndesis-s2i:latest`

```bash
docker images

REPOSITORY              TAG                 IMAGE ID                  
syndesis/syndesis-s2i   latest              e27b19a7717d               
syndesis/syndesis-s2i   1.6.7               e556ebf9d6b9                 
```

You can now run the integration tests and they will use that local Syndesis version.

## Use release versions

By default the integration tests run with the latest Syndesis SNAPSHOT version. This usually is the latest SNAPSHOT version built on your local host. In case you do not have
the latest SNAPSHOT version built on your machine you may want to explicitly specify a release version of Syndesis. The tests will then run with that particular version of Syndesis as
a system under test.

All required artifacts and Docker images are loaded form Maven central and Dockerhub so you might bring some time for that pull to finish. But once you have the versions loaded subsequent
build just use the already downloaded artifacts.

You can specify the Syndesis version as system property:

```bash
mvn clean verify -Dsyndesis.version=1.6.7
```

The comand above runs the tests with the Syndesis release version `1.6.7`. 

Here is a list of available releases: [https://github.com/syndesisio/syndesis/releases](https://github.com/syndesisio/syndesis/releases)

Syndesis also provides a daily release build that can be used for continuous integration.

## Syndesis integration runtime container

Syndesis executes integrations with a special runtime container. The container is usually provided with a generated integration project holding all sources required to run the
integration (such as integration.json, pom.xml, atlas-mappings, application.properties, secrets and so on). The integration runtime container usually builds from the `syndesis/syndesis-s2i:latest` 
Docker image that brings all required Syndesis artifacts and required 3rd party libs.

The integration tests provide a Testcontainer that represents the integration runtime container. You can add the integration runtime container to your tests in following ways.

First of all you can use a JUnit class rule and add the container to your test. 

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-export")
                .fromExport(TimerToLog_IT.class.getResourceAsStream("TimerToLog-export.zip"))
                .build();
```

This creates a new Syndesis runtime container and starts the integration from an export file `TimerToLog-export.zip`. This integration runtime container is shared for all test methods 
in that test class. 

In case you want the runtime container to be part of a test method you can just initialize the container and start it by yourself.

```java
@Test
public void timeToLogExportTest() {
    SyndesisIntegrationRuntimeContainer.Builder integrationContainerBuilder = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("timer-to-log-export")
            .fromExport(TimerToLog_IT.class.getResourceAsStream("TimerToLog-export.zip"));

    try (SyndesisIntegrationRuntimeContainer integrationContainer = integrationContainerBuilder.build()) {
        integrationContainer.start();
    
        //do something with the integration runtime container
    }
}   
``` 

The `try-with-resources` block ensures that the container is stopped once the test is finished.

You can start several runtime containers within a test. The container uses an integration source that defines the integration logic. You ca use multiple sources
of integrations to build the container.

### From integration export

You can run exported integrations in the runtime container. This is the most convenient way to start the integration as every information required to run the integration is bundled in the
export file. You can customize the integration properties though using [integration customizers](#customize-integrations).

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-export")
                .fromExport(TimerToLog_IT.class.getResourceAsStream("TimerToLog-export.zip"))
                .build();
```
 
### From integration model

You can create the integration model and run that integration in the runtime container. The integration model can be seen easily within the test and you can
create variations of that integration for instance when using a parameterized test.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
    .name("timer-to-log")
    .fromFlow(new Flow.Builder()
            .steps(Arrays.asList(new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .connection(new Connection.Builder()
                        .id("timer-connection")
                        .connector(new Connector.Builder()
                            .id("timer")
                            .putProperty("period",
                                new ConfigurationProperty.Builder()
                                        .kind("property")
                                        .secret(false)
                                        .componentProperty(false)
                                        .build())
                            .build())
                        .build())
                    .putConfiguredProperty("period", "1000")
                    .action(new ConnectorAction.Builder()
                        .id("periodic-timer-action")
                        .descriptor(new ConnectorDescriptor.Builder()
                            .connectorId("timer")
                            .componentScheme("timer")
                            .putConfiguredProperty("timer-name", "syndesis-timer")
                            .build())
                        .build())
                    .build(),
                new Step.Builder()
                    .stepKind(StepKind.log)
                    .putConfiguredProperty("bodyLoggingEnabled", "false")
                    .putConfiguredProperty("contextLoggingEnabled", "false")
                    .putConfiguredProperty("customText", "Hello Syndesis!")
                    .build()))
            .build())
        .build();
``` 

### From integration fat jar 

If you have an integration project fat jar available you can build the integration runtime container directly with that project jar file.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-jar")
                .fromFatJar(Paths.get("/path/to/syndesis-project.jar"))
                .build();
```

### From integration project 

You can build a runtime container from a Syndesis integration project folder. The project should contain all resources required to run the integration.
The integration runtime container will use a volume mount to that directory.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-dir")
                .fromProjectDir(Paths.get("/path/to/project-dir"))
                .build();
```

### From integration json 

You can also provide the integration Json model file directly.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-json")
                .fromJson(TimerToLog_IT.class.getResourceAsStream("TimerToLog.json"))
                .build();
```

### From integration source 

Last not least you can provide an integration source implementation representing to build an run in the container.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                .name("timer-to-log-json")
                .fromSource(new JsonintegrationSource(TimerToLog_IT.class.getResourceAsStream("TimerToLog.json")))
                .build();
```

### Runtime S2i execution modes

The integration runtime container takes an integration source and builds the container with the `syndesis/syndesis-s2i:latest` image as base. The S2i image
traditionally uses a run script to execute fat project jars. The project fat jar is built before in an assemble step. This is close to production where the same
mechanism applies to integrations that get published in Openshift.

On the downside the S2i image build takes some more time to finish assembling and running the integration project as it required two Docker containers to run separately.
The first Docker container performs the assemble step and the 2nd container executes the built fat jar.

This close to production S2i mechanism is enabled with the system property **syndesis.s2i.build.enabled=true**.

By default this mechanis is disabled in order to gain some more speed in test execution. When the S2i mode is disabled the integration runtime container will
directly execute the project with `mvn spring-boot:run`. Still the integration is run inside using the `syndesis/syndesis-s2i:latest` base image but the assemble step
is skipped and we do not execute the fat jar with `java -jar`. Instead a the Spring Boot maven plugin is used.   

## Syndesis db container

Syndesis uses a database to store integrations and connections in a Postgres storage. The integration tests provide a Postgres Testcontainer that is
configured with the proper `syndesis` database and user.

In addition to that the container defines some `sampledb` database holding two tables `todo` and `contact`. These sample tables are used by integrations when
testing data persistence with SQL connectors.

The integration tests automatically start the `syndesis-db` Testcontainer. All tests share the same container.

In case an integration runtime container needs access to the database you can add container networking as follows:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                        .name("webhook-to-db")
                        .fromExport(WebHookToDB_IT.class.getResourceAsStream("WebhookToDB-export.zip"))
                        .build()
                        .withNetwork(getSyndesisDb().getNetwork())
                        .withExposedPorts(SyndesisIntegrationRuntimeContainer.SERVER_PORT);
```

You can use `withNetwork(getSyndesisDb().getNetwork())` to access the database from a running integration. The `syndesis-db` container uses proper network aliases to ensure that
the integration is able to connect using the default SQL connector settings.

## Syndesis server container

This container starts the Syndesis backend server. The container connects to the database container and provides REST services usually called via the Syndesis UI. The
container starts with some default properties set:

```
encrypt.key=supersecret
controllers.dblogging.enabled=false
openshift.enabled=false
metrics.kind=noop
features.monitoring.enabled=false
```

You can add/overwrite settings while creating the container:

```java
@ClassRule
public static SyndesisServerContainer syndesisServerContainer = new SyndesisServerContainer.Builder()
        .withJavaOption("encrypt.key", "something-different")
        .build()
        .withNetwork(getSyndesisDb().getNetwork());
```

By default the server container uses the Docker image `syndesis/syndesis-server:latest`. You can customize the image tag that should be used in order to start a different
release version of the Syndesis backend server. The integration test will pull the Docker image if not present on your host. See the list ov available 
[image tags for syndesis-server](https://hub.docker.com/r/syndesis/syndesis-server/tags).

When building a local server version you can also provide the path to a local `syndesis-server.jar`:

```java
@ClassRule
public static SyndesisServerContainer syndesisServerContainer = new SyndesisServerContainer.Builder()
        .withClasspathServerJar("path/to/server-runtime.jar")
        .build()
        .withNetwork(getSyndesisDb().getNetwork());
```

Instead of building the `server-runtime.jar` on your own you can also copy the jar from Maven central or your local Maven repository.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
      <execution>
        <id>copy</id>
        <phase>prepare-package</phase>
        <goals>
          <goal>copy</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <skip>false</skip>
      <artifactItems>
        <artifactItem>
          <groupId>io.syndesis.server</groupId>
          <artifactId>server-runtime</artifactId>
          <version>${syndesis.version}</version>
          <type>jar</type>
          <outputDirectory>${project.build.testOutputDirectory}</outputDirectory>
          <destFileName>server-runtime.jar</destFileName>
        </artifactItem>
      </artifactItems>
    </configuration>
</plugin>        
```

This copies the `server-runtime.jar` with version `${syndesis.version}` to the test output directory. Now you can use the jar in your test using `withClasspathServerJar("server-runtime.jar")`.

## Infrastructure containers

### AMQ message broker

Some integrations connect to a AMQ message broker. The integration test project provides a JBoss AMQ container that is ready to be used with integration runtimes. In case
your integration requires the message broker you can add it to the test as JUnit class rule as follows:

```java
@ClassRule
public static JBossAMQBrokerContainer amqBrokerContainer = new JBossAMQBrokerContainer();
```

The AMQ broker container exports following ports and services:

```
withExposedPorts(61616);//openwire
withExposedPorts(61613);//stomp
withExposedPorts(5672);//amqp
withExposedPorts(1883);//mqtt
withExposedPorts(8778);//jolokia
```

In case your integration requires access to the message broker container you should add a networking to the container when building the integration runtime container. 
As usual this is done using `withNetwork(amqBrokerContainer.getNetwork())` configuration:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("amq-to-http")
        .fromExport(AMQToHttp_IT.class.getResourceAsStream("AMQToHttp-export.zip"))
        .customize("$..configuredProperties.baseUrl",
                    String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, todoServerPort))
        .build()
        .withNetwork(amqBrokerContainer.getNetwork());
```

## Simulate 3rd party interfaces

Many integrations connect to the outside world consuming services that are provided by 3rd party vendors (such as Twitter, Google, Salesforce, etc.) When testing those
integrations we need to simulate the 3rd party services as we do not want the integration tests to connect to the real 3rd party services. 

The integration tests use Citrus as base simulation framework for this task.

You can add Citrus components (Http services, AMQ consumers and so on) to your tests using Spring configurations:

```java
@Configuration
public static class EndpointConfig {
    @Bean
    public HttpServer todoApiServer() {
        return CitrusEndpoints.http()
                .server()
                .port(todoServerPort)
                .autoStart(true)
                .timeout(60000L)
                .build();
    }
}
```

The sample above creates a Citrus Http server listening on a dynamic tcp port. As we want to connect to that simulated service from within the integration runtime container we
need to expose this port to the Testcontainer runtime:

```java
private static int todoServerPort = SocketUtils.findAvailableTcpPort();
static {
    Testcontainers.exposeHostPorts(todoServerPort);
}
```

Testcontainers such as our integration runtime container can now connect to the port using the special host name `host.testcontainers.internal`:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("http-to-http")
        .fromExport(HttpToHttp_IT.class.getResourceAsStream("HttpToHttp-export.zip"))
        .customize("$..configuredProperties.baseUrl",
                    String.format("http:/host.testcontainers.internal:%s", todoServerPort))
        .build();
```

As you can see we customize the integration configured properties to use the simulated Citrus service endpoint as base URL. This way the integration connects to the
Citrus service instead of calling the real production endpoint.

The Citrus components usually provide services that are used by the integrations in order to control/verify the exchanged data. You tell the Citrus components what data to expect 
and return with the test runner Java DSL. As the base integration test is using Citrus functionality we can just inject the test runner to the test method.

```java
@Test
@CitrusTest
public void testHttpToHttp(@CitrusResource TestRunner runner) {
    runner.http(builder -> builder.server(todoApiServer)
            .receive()
            .get("/todos"));

    runner.http(builder -> builder.server(todoApiServer)
            .send()
            .response(HttpStatus.OK)
            .payload("[{\"id\": \"1\", \"task\":\"Learn to play drums\", \"completed\": 0}," +
                      "{\"id\": \"2\", \"task\":\"Learn to play guitar\", \"completed\": 0}," +
                      "{\"id\": \"3\", \"task\":\"Important: Learn to play piano\", \"completed\": 0}]"));
}
```

The test expects an incoming `GET` request on `/todos` on the simulated Citrus service. Citrus the is supposed to respond with a sample list of todo tasks.

This way wen can control the test data returned by 3rd party services and we implicitly validate that the integration connects to the 3rd party services.

## Customize integrations

### URLs and destinations

The exported integration may connect to 3rd party services. The services to connect to are defined with base URLs in the export. We have to overwrite
these URLs for the integration test because we want the integration to connect to a simulated 3rd party service instead of the production endpoint.

You can overwrite any configured property in the integration export using JsonPath expressions:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("http-to-google-sheets")
        .fromExport(HttpToHttp_IT.class.getResourceAsStream("HttpToGoogleSheets-export.zip"))
        .customize("$..configuredProperties.baseUrl",
                String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, todoServerPort))
        .customize("$..rootUrl.defaultValue",
                String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, googleSheetsServerPort))        
        .build();
```

While building the integration runtime container we can add JsonPath expressions that customize the exported integration. This enables us to set any configured
property in the integration export. In the sample above we overwrite the Http service base URL that is periodically called as start connection. In addition to that
we overwrite the Google Sheets root URL and point to the local simulated 3rd party services.

The Google Sheets connection also uses encrypted user credentials and secrets in the integration export. These credentials get automatically overwritten before the test.

### Encrypted secrets

The integration exports may use encrypted credentials representing passwords and secrets for connections to 3rd party services. The integration test 
automatically overwrites the encrypted values with a static secret ("secret"). This way simulated databases, infrastructure components (such as message brokers)
and 3rd party services can use the default "secret" credential in order to word with the integration export.

## Scheduler expressions

Sometimes integrations get periodically invoked with timer or scheduler. The exported integrations may use minutes or hours delay settings which is not
applicable to automated tests. You can overwrite the timer period with integration customizers:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("http-to-amq")
        .fromExport(HttpToAMQ_IT.class.getResourceAsStream("HttpToAMQ-export.zip"))
        .customize("$..configuredProperties.schedulerExpression", "1000")
        .customize("$..configuredProperties.baseUrl",
                    String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, todoServerPort))
        .build()
        .withNetwork(amqBrokerContainer.getNetwork());
```

The expression `customize("$..configuredProperties.schedulerExpression", "1000")` overwrites the scheduler to fire every 1000 milliseconds. This will be more
sufficient to the automated integration tests in terms of avoiding long running tests.

## Logging

By default the integration tests log output to a file `target/integration-test.log`. You can also enable logging to the console in `src/main/resources/logback-test.xml`.

When running containers the log output is not visible by default. You need to enable logging on the container:

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("http-to-http")
        .fromExport(HttpToHttp_IT.class.getResourceAsStream("HttpToHttp-export.zip"))
        .enableLogging()
        .build();
``` 

The `enableLogging` setting enables container logging to the logback logger. By default the container logs are sent to a separate log file `target/integration-runtime.log`.

You can als enable logging globally by setting the system property **syndesis.logging.enabled=true**. All containers will use logging and the container output is
printed to the respective log file appender.

## Debugging

We can start the integration runtime container with debug mode enabled. This exposes a debug port that your favorite IDE can connect to with a remote debug session.

```java
@ClassRule
public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("http-to-http")
        .fromExport(HttpToHttp_IT.class.getResourceAsStream("HttpToHttp-export.zip"))
        .enableDebug()
        .build();
``` 

The only thing we have to do is to add the `enableDebug` option to the integration runtime container. The container will be suspended waiting for a client to open
the remote debug session using the specified debug port. The debug port (default=5005) is exposed to the Docker host using the very same port. This means that you can instruct your IDE to
open a new debugging session to `localhost:5005`. In case you need to change the debug port you can use the following system property or environment variable setting:

* **syndesis.debug.port**
* **SYNDESIS_DEBUG_PORT**

We can pass the debug port as system property to the Maven build, too. It gets automatically set as system property in the maven-failsafe test JVM.

```bash
mvn clean verify -Dsyndesis.debug.port=5005
```

You can enable debug options globally by setting the system property **syndesis.debug.enabled=true**. This is useful when executing a single test with debug mode enabled via
Maven system property:

```bash
mvn clean verify -Dit.test=MyTestClassName#mytestMethodName -Dsyndesis.debug.enabled=true
```

