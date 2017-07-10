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

When Apache Camel startup then Camel routes will startup by default, and as part of their startup then the (route consumers) will often connect to remote systems,
and in case of connection errors those will be thrown as exception and Camel will fail to startup. Only a handful of Camel components has built-in failover/retry in those situatations.

For Syndesis we would need to be in full control, and therefore we will need to ensure Apache Camel can startup even if there is a connection problem with a remote system. This requires to configure Apache Camel to not auto start the routes, which can be done in Spring Boot by configuring `application.properties` with the following:

    camel.springboot.auto-startup = false

By doing so no routes is automatic startup up, but we would then need to defer starting these routes and do this using another way. Therefore we implement a new `RouteController` which will be responsible for starting up the routes in a controlled manner.

The `RouteController` will be bootstrapped via the `RoutePolicyFactory` where we in the `onInit` callback can register the routes to the `RouteController` which then
runs as a background thread and orchestrates starting the routes. The `RouteController` will then be responsbible for starting up all these routes, and have support for periodically retry in case a route fails to startup. And only when all routes has been started the `RouteController` has completed starting up all the routes.

We would need to make the discovery and bootstrap of the `RouteController` very easy, which we can do using Camel's existing `RoutePolicyFactory`.

In Camel with Spring Boot we can setup this in the `Main` class via something along the following code lines:

    @Bean
    RouteController newRouteController(CamelContext camelContext) {
        return new RouteController(camelContext);
    }

    @Bean
    RoutePolicyFactory newRouteController(RouteController controller) {
        return new RouteControllerRoutePolicyFactory(controller);
    }

Or:

    @Bean
    RouteController newRouteController(CamelContext camelContext) {
        // The route controller is also in charge to add add its own
        // RoutePolicyFactory to the camel context
        return new RouteController(camelContext);
    }

There should be one shared instance of `RouteController` which is given to the `RoutePolicyFactory`, which then creates a `RoutePolicy` for each route that will be controlled.

TODO: We could make `RouteController` an SPI in Apache Camel and provide it OOTB in Apache Camel. Then we can have Camel auto detect if its enabled on startup
and then automatic set `autoStartup = false`, and then let the `RouteController` startup the routes (We have this idea for Camel 3.0). This can then make it even
easier to enable. The `RouteController` should have configuration setting to specify how often to retry starting failed routes (we may need to have backoff etc).
We can then implement a `DefaultRouteController` in camel-core, and then make it easy to use by setting `CamelContext.setRouteControllerEnabled(true)` (find a good name). With Camel on Spring Boot then its a matter of setting this in the `application.properties` via `camel.springboot.route-controller.enabled = true`.

Besides starting up routes the `RouteController` should have a Java, JMX API, and REST which can report back status of the running integrations (i.e. running routes).
At minimum it should collect status of each route with details:

- route id
- current status (starting, started, stopping, stopped)
- health (unknown, ok, failure)
- failure error message (exception message + stacktrace)

TODO: API details to be determined

To expose this API in Rest we can use Spring Actuate which allows to expose such details. For en example see `org.apache.camel.spring.boot.actuate.endpoint.CamelRoutesEndpoint`

The `RouteController` should also have APIs to force starting a route on demand, eg so an user can click a button, and then it will immediately schedule to run a task that attempts to start the route.

The `RouteController` will have a `ScheduledExecutorService` where we schedule tasks to attempt to start the routes.

