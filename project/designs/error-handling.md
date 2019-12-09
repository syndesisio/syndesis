## Error Handling in Integration Flows

* Issue: https://issues.redhat.com/browse/ENTESB-11675
* Sprint: 56
* Target audience: Connector Developers

### Introduction

At runtime an integration can run into an error condition. In most cases we simply log and report the error but in cases where the integration flow was invoked by an external caller it makes sense to return an error response back to that caller. In particular we are talking about the api-provider and webhook connector. These technologies should be able to response with an HTTP Status code if an error condition occurs.

In this design we work to provide the integration developer with a mapping screen which allows mapping of errors to HTTP Status codes.


### Errors and Error Categories

Many types of errors can occur so it makes sense to not expose the integration developer to each and everyone of these errors as this might become an unmanageble list. Instead it makes sense to group then into error categories. Each connector can define their own, for example the SQL connector defines


```
    public static final String SQL_CONNECTOR_ERROR        = "SQL_CONNECTOR_ERROR";
    public static final String SQL_DATA_ACCESS_ERROR      = "SQL_DATA_ACCESS_ERROR";
    public static final String SQL_ENTITY_NOT_FOUND_ERROR = "SQL_ENTITY_NOT_FOUND_ERROR";
```
See https://github.com/syndesisio/syndesis/blob/master/app/connector/sql/src/main/java/io/syndesis/connector/sql/customizer/SqlErrorCategory.java

and an `Standardized Error` should be set in the Connector's description

```
    "standardizedErrors": [
      {
        "name": "SQL_DATA_ACCESS_ERROR",
        "displayName": "SqlDataAccessError"
      },
      {
        "name": "SQL_ENTITY_NOT_FOUND_ERROR",
        "displayName": "SqlEntityNotFoundError"
      },
      {
        "name": "SQL_CONNECTOR_ERROR",
        "displayName": "SqlConnectorError"
      }
```
https://github.com/syndesisio/syndesis/blob/master/app/connector/sql/src/main/resources/META-INF/syndesis/connector/sql.json#L8-L20

In the `doBefore` and `doAfter` customizers these error categories can be used to throw a `SydnesisConnectorException` for example

```
    if (isRaiseErrorOnNotFound && !isRecordsFound(in))  {
        String detailedMsg = "SQL " + statementType.name() + " did not " + statementType +  " any records";
        throw new SyndesisConnectorException(SqlErrorCategory.SQL_ENTITY_NOT_FOUND_ERROR, detailedMsg);
    }
```
See https://github.com/syndesisio/syndesis/blob/master/app/connector/sql/src/main/java/io/syndesis/connector/sql/customizer/SqlConnectorCustomizer.java#L123-L125

There is a small problem however, some (runtime) exceptions stop execution of the pipeline and the `doAfter` customizer will not be called. In this case the connector developer is forced to add the specific exception that needs to be handeled to the `standardizedErrors` section like so

```
    {
        "name": "org.springframework.dao.DuplicateKeyException",
        "displayName": "SqlDuplicateKeyError"
    },
```

### Define an Error Handler

In your connector descriptor  you can define an error handler class which is called when an error occurs. A good example can be found in the api-provider (https://github.com/syndesisio/syndesis/blob/master/app/connector/api-provider/src/main/resources/META-INF/syndesis/connector/api-provider.json)

```
    "onException": "io.syndesis.connector.apiprovider.ApiProviderOnExceptionHandler",
```

Where in the case of the api-provider's endStep the error is mapped to an HTTP Status code. 

https://github.com/syndesisio/syndesis/blob/master/app/connector/api-provider/src/main/java/io/syndesis/connector/apiprovider/ApiProviderOnExceptionHandler.java

Another example can be found in the WebHook Connector.

Note that it is the RouteBuilder that adds the Camel onException clause to your route (https://camel.apache.org/manual/latest/exception-clause.html). If a we have more then on connector with an `onException` definition the last one in the flow wins.

In the RouteBuilder look for the following code

```
    Optional<String> onException = Optional.empty();
    Step onExceptionStep = null;
    for (Step step : flow.getSteps()) {
        final ConnectorDescriptor descriptor =
                step.getActionAs(ConnectorAction.class).get().getDescriptor();
        if (descriptor.getOnException().isPresent()) {
            onException = descriptor.getOnException();
            onExceptionStep = step;
        }
    }
    if (onException.isPresent() && onExceptionStep!=null) {
        //
        final OnExceptionDefinition onExceptionDef = new OnExceptionDefinition(Throwable.class)
                .handled(true)
                .maximumRedeliveries(0);

        final Processor errorHandler = (Processor) mandatoryLoadResource(
                this.getContext(), "class:" + onException.get());
        ((Properties) errorHandler).setProperties(onExceptionStep.getConfiguredProperties());

        final DefaultErrorHandlerBuilder builder = new DefaultErrorHandlerBuilder();
        builder.setExceptionPolicyStrategy((exceptionPolicies, exchange, exception) -> onExceptionDef);
        builder.setOnExceptionOccurred(errorHandler);

        rd.setErrorHandlerBuilder(builder);
    }
```

### Adding the ErrorMapper Configuration Widget to your action configuration

Finally we want the user to be presented with an ErrorMapper Widget so they can configure the appropriate mapping. For this to happen configure a property with name `errorResponseCodes` in the connectors descriptor json:


```
    "propertyDefinitionSteps": [
      {
        "description": "API Provider Return Path Configuration",
        "name": "configuration",
        "properties": {
          "errorResponseCodes": {
            "componentProperty": false,
            "deprecated": false,
            "defaultValue": "{}",
            "description": "The return code to set according to different error situations",
            "displayName": "Error Response Codes",
            "javaType": "Map",
            "kind": "parameter",
            "required": false,
            "secret": false,
            "type": "mapset"
          }
        }
      }
    ]
```

You can see this Widget in action in Figure 3 of the api-provider quickstart:  https://github.com/syndesisio/syndesis-quickstarts/blob/master/api-provider/README.md

### Final notes

Thus far we have handlers for HTTP responses only (api-provider and webhook) to map to HTTP status codes. Going forward we will likely extend this to other types of error handlers.
