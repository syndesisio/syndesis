# Connection Tester

The goal of this document is to sketch out possible approaches for implementing _Connection testing_ in the context of iPaaS. This backend concept is introduced to support the use case to quickly test whether a given `Connection`  (which is the combination of a `Connector` and a `ConnectorConfiguration`) is actually usable. The typical use case is, that the user pushes a button "Test connection" on the "Connection Details" page which actually triggers the test of the connection and that can be either successful or failing. When it fails, a detailed error message describes not only the error itself but also how to resolve the error (e.g. "Cannot connect because of invalid credentials"). 

## Communication flow

When the user triggers this test function from within the UI, a backend call to `ipaas-rest` will be issued, which in turn will trigger a (still to be defined) method on the referenced connector with the concrete configuration given. This method, let's call it `ping()`, must be side effect free, which should be possible for most endpoints. The method takes the configuration as argument for the connector and returns a result objects which contains the result of the check (ok / nok) and in case of a failure the error message (question: Should we already think about I18N here ?)

## Side effect free connection test

Endpoints like database or REST endpoints can be checked side-effect free (e.g. for HTTP based services on could try a `HEAD` request to the root context). If this is not possible because e.g. some payload would be needed to be transferred in order to determine whether the connection works, then a connection test in the sense described here can't be performed. Instead some "sample flow run" needs to be performed which moves a test payload through the flow where the connector is at the the end (but this should be also represented differently on the UI).

Its important that the UI knows whether a connection is testable or not so that it can provide the appropriate UI state (e.g. an enable or disabled test button). Therefore each `Connector` has an extra `canPing()` method to allow to query for this information. `ipaas-rest` stores this information along with the `Connection` object which is handed out to the UI for showing the connection (along with the 'test' button). The method `canPing()` should receive the same `ConnectorConfiguration` as `ping()` itself so that it can dynamically determine whether a ping is possible. By default this argument to `canPing()` is probably ignored since a connector's singable-attribute is unique to the type of connector and independent of connection parameters.

In summary, `ping()` and `canPing()` are suggested as additions to the connector interface within the upstream Camel connector API definitions.

## Connection Tester Runtime

It is essential, that connection testing happens as fast as possible as it is a synchronous operation. The initial trigger is a HTTP request from `ipaas-ui` to `ipaas-rest` which then performs the test action against the backend. A naive approach would be to spin up a Java pod with the referenced connector, call the `ping()` method, return the result to the client and (asynchronously) tears down the pod. However, the startup time are considered to be too expensive, to that it make sense to have a pool of pods running which can be directly contacted to perform the ping check. Various scenarios are conceivable:

* **All-in-one** pods which can perform the ping check for all connectors. These would have access to all connectors supported and expose a service which has the connector type as parameter as well as the `ConnectorConfiguration` for calling out to the connector. A single K8s service is in front of these pods which is called by the `ipaas-rest`. This will work only if all connectors are known upfront or connectors could be loaded dynamically. 
* **Dedicated** pods which holds only one connector each. Each of this pod has a different service as entry point (whose name is associated with the connector name). So there is one pod, one service and `ipaas-rest` will discover the proper service (by naming convention to the connector name) and call it with the `ConnectorConfiguration`. The advantage is, that if no such service could be found, the `ipaas-rest` could install the service on the fly. This works also for connectors which are not known upfront but might be defined later (e.g by expert developers).
* **Mixed** pods which hold a set of connectors but also know how to dynamically load new connectors. For each connector it add a label or annotation to this pod. This set of labels (where the connector type is part of the key name) can be queried from the outside. Services can be defined to select on these labels, so that there could be also one service per connector which has a single label as selector matching all pods which have this connector available. Again, if there is no matching service or the service does not match any pod, then a new pod can be fired up dynamically, containing the requested connector. The main point here is, that the connector types are attached as labels, e.g. `connector.twitter.query=1, connector.salesforce.create-contact=1`, ... This is probably the most flexible one as it can be also scaled independently. The challenge is how to load connectors dynamically from a central (Nexus) repository, how to update the Pods meta-data and how to dynamically create services.

On a first glance, the fist **all-in-one** option is the easiest one, as it only needs a single service, where the pods behind will either already have all connectors preloaded or can load dynamically.

## Open Questions

* Is the set of connectors static or can these be defined by _expert developers_, too ?
* If connectors can be defined dynamically, how ...
    * ... are these build ?
    * ... are they distributed ? (via Maven repo ?)
    * ... how can they be loaded by the tester pods ?
* How should I18N works for the error messages returned ? Is each connector responsible for I18N (and the locale should be part of the API) ? Or is I18N out of scope ?
* Can connectors be tested with out a Camel route ? (would be the easiest)

## API suggestion

```java
interface Pingable {
    PingResult ping(ConnectionConfiguration config);
    boolean canPing(ConnectionConfiguration config);
}

class PingResult {
 private boolean isSuccess();
 private String errorMessage();
}
```

