Syndesis - Camel Health Check
=============================================

* Issues:
  * https://github.com/syndesisio/syndesis-project/issues/42
  * https://issues.apache.org/jira/browse/CAMEL-11443

* Branches:
  * https://github.com/lburgazzoli/apache-camel/tree/CAMEL-11443

## Background

There is two kind of health checks required for Syndesis:

* Technical health checks which ensure that the Integration itself is running, but not whether the components are without faults. This healthcheck is used as liveness and readiness checks for OpenShift
* Status of an integration with respect to the backends. This status should be visualised in the Syndesis UI to give direct feedback to the user. It must not be the case that a faulty backend restarts the integration.

### Technical health checks

These are native OpenShift readiness and liveness probes.

It is important that the readiness and liveness probes are only being used casually to ensure the integration can startup and become ready, and that the JVM stays alive. We can leverage the existing Camel health check from camel-spring-boot (`/health` HTTP REST endpoint), that ties into OpenShift readiness/liveness probes and ensures the JVM, Spring-Boot, and Apache Camel can startup and be ready. The livensss probe then calls the same `/health` HTTP REST endpoint which just checks that Spring Boot, and Camel is running.

We do not want the OpenShift liveness probe to react on errors from the integration because when an integration is in an error state because of one of its connectors fails, this should be reported by other means than Kubernetes status changes. It should be visible on the Syndesis UI instead (i.e. integration health checks).

### Integration health checks (status)

These are Syndesis specific and requires new functionality developed into Apache Camel and as well in Syndesis.

## Implementation

The existing Camel health check from camel-spring-boot (`/health` HTTP REST endpoint) can be used as-is, however as we want Syndesis to be more in control of errors, we would need to ensure that the readiness/liveness probes reports OK even if an integration startup and cannot connect to a remote system, and would be in error.

When Apache Camel startup then Camel routes will startup by default, and as part of their startup then the (route consumers) will often connect to remote systems, and in case of connection errors those will be thrown as exception and Camel will fail to startup. Only a handful of Camel components has built-in failover/retry in those situatations.

For Syndesis we would need to be in full control, and therefore we will need to ensure Apache Camel can startup even if there is a connection problem with a remote system. This requires to configure Apache Camel to not auto start the routes, which can be done in Spring Boot by configuring `application.properties` with the following:

    camel.springboot.auto-startup = false

By doing so no routes is automatic startup up, but we would then need to defer starting these routes and do this using another way.

When using spring,the camel context can be configured using beans from the registry [1] therefore we can add a new auto discovered `RouteController` SPI which will be the responsible for starting up the routes in a controlled manner.

NOTE: as the idea of a route controller has been brought up for Camel 3.0, the  RouteController will be an early preview SPI.

NOTE: as refactoring the route mamagement requires to change camel-core, the first implementation leverages `RoutePolicy`/`RoutePolicyFactory`

The `RouteController` implementation will sets `autoStartup = false` and will install its own `RoutePolicyFactory` which then creates a `RoutePolicy`  for each route an where we in the `onInit` callback can register the routes to the `RouteController` which then runs as a background thread and orchestrates starting the routes. The `RouteController` will then be responsbible for starting up all these routes, and have support for periodically retry in case a route fails to startup. And only when all routes has been started the `RouteController` has completed starting up all the routes.

In Camel with Spring Boot we can setup this in the `Main` class via something along the following code lines:

```java
    @Bean
    RouteController newRouteController(CamelContext camelContext) {
        // The route controller is also in charge to add add its own
        // RoutePolicyFactory to the camel context
        return new RouteController(camelContext);
    }
```    


TODO:
- The `RouteController` should have configuration setting to specify how often to retry starting failed routes (we may need to have backoff etc).
- The `RouteController` could have an optional `ScheduledExecutorService` where we schedule tasks to attempt to start the routes.
- We can then implement a `DefaultRouteController` in camel-core, and then make it easy to use by setting `CamelContext.setRouteControllerEnabled(true)` (find a good name). With Camel on Spring Boot then its a matter of setting this in the `application.properties` via `camel.springboot.route-controller.enabled = true`.

Besides starting up routes the `RouteController` should have a Java, JMX API, and REST which can report back status of the running integrations (i.e. running routes) and should also have APIs to force starting a route on demand, eg so an user can click a button, and then it will immediately schedule to run a task that attempts to start the route.

At minimum it should collect status of each route with details:

- route id
- current status (starting, started, stopping, stopped)
- health (unknown, ok, failure)
- failure error message (exception message + stacktrace)

TODO: API details to be determined

To expose this API in Rest we can use Spring Actuate which allows to expose such details and leverage:
- `org.apache.camel.spring.boot.actuate.endpoint.CamelRoutesEndpoint`
- `org.apache.camel.spring.boot.actuate.endpoint.CamelRoutesMvcEndpoint`

The MVC endpoint could expose the following api:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| GET | /camelroutes | List all the routes with minimal information |
| GET | /camelroutes/{id}/info | Provide detailed information about the route identified by {id} |
| POST | /camelroutes/{id}/start | Attempt to start the route identified by {id} |
| POST | /camelroutes/{id}/stop | Attempt to stop the route identified by {id} |

An example of what /camelroutes could return is:

```json
[
  {
    "id": "bar",
    "uptime": "10.347 seconds",
    "uptimeMillis": 10347,
    "status": "Started"
  },
  {
    "id": "foo",
    "uptime": "10.341 seconds",
    "uptimeMillis": 10341,
    "status": "Started"
  },
  {
    "id": "undertow",
    "uptimeMillis": 0,
    "status": "Stopped"
  }
]
```

NOTE: the path could be changed like endpoints.camelroutes.path = /camel/routes

=== References
- [1] http://camel.apache.org/advanced-configuration-of-camelcontext-using-spring.html
