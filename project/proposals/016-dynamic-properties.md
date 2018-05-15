# Support for dynamic properties

* Issue: https://github.com/syndesisio/syndesis-project/issues/16
* Sprint: 15
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

## Background

For some connectors it's difficult or cumbersome for users to specify property values, for instance there could be many property values that user can choose from, e.g. Salesforce Object types, SQL stored procedure names, or without offering possible values user has a hard time determining what would be the best property value to input (e.g. what unique field to use with create or update action with Salesforce). Having a fixed predefined set of property values is not feasible as they can differ between users, organizations or versions, i.e. they must be determined at runtime.

With some of the properties depending on each other, e.g. unique field for create or update action with Salesforce depends on the Salesforce object type, UI needs to provide a multistep interface for defining action properties -- e.g. picking Salesforce object type needs to be done before picking unique field of that object type, the backend needs to provide action metadata with that information.

This functionality will give support to UI for implementing multistep interface with a pick list, auto-complete or property filter functionality for the values determined at runtime from the metadata with respect to the chosen connector/connection.

## User Story

As a citizen user, I would like to pick property values instead of or in addition to manually inputting them in order to make it easier to select appropriate values or make fewer mistakes when selecting particular value in particular context.

For example when using Salesforce's connector action _create or update_ I need to firstly select _Salesforce object type_ I wish to create or update and then a _unique field_ of that type used to check for duplicates. Or when using SQL stored procedure connector action _invoke stored procedure_ I need to select the stored procedure to invoke.

## Domain

This proposal changes the action properties to contain additional metadata with a list of steps that group action properties that need to be presented piecemeal.

`properties` map is replaced with `ActionDefinition` and input/output data shapes are not mandatory to reflect that they are determined by the selection of action properties:
```diff
public interface Action extends WithId<Action>, WithName, Serializable {

-  DataShape getInputDataShape();
+  Optional<DataShape> getInputDataShape();

-  DataShape getOutputDataShape();
+  Optional<DataShape> getOutputDataShape();

  //...
+  ActionDefinition getDefinition();
  //...
}
```

`ActionDefinition` is introduced to contain the `Step`s:
```java
public interface ActionDefinition {

  interface Step extends WithName, WithProperties {

    String getDescription();

  }

  List<Step> getPropertyDefinitionSteps();

  Optional<DataShape> getInputDataShape();

  Optional<DataShape> getOutputDataShape();
}
```

Action property values are suggested by extending `WithProperties` is with an `enum` property:

```diff
public interface ConfigurationProperty extends WithTags {
  // ...
+  List<PropertyValue> getEnum();
  // ...
}
```

```java
public interface PropertyValue {

  String getValue();

  String getLabel();

}
```

`enum` property will hold any value suggestions that make sense for the currently selected action properties. The list could hold zero or more suggested value-label pairs depending on the connector. Backend should provide as much property value suggestions as it is feasible, but the frontend should adapt if the number of suggestions changes, e.g. having multiple suggestions reduced to zero or vice versa.

`DataShape` is extended to hold an embedded specification, for instance JSON or XML schema.

```diff
public interface DataShape extends Serializable {
  // ...
+  String getSpecification();
  // ...
}
```

## API

To interact with the dynamic parameter API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| POST      | /api/{version}/connections/{connectionId}/actions/{actionId}      | Fetches the action metadata for given action id, i.e. the `ActionDefinition` model detailed above |

The `/api/{version}/connections/{connectionId}/actions/{actionId}` endpoint receives a map of key-value pairs of currently selected properties by the end user. These properties are the ones that will be submitted as `configuredProperties` properties of `Integration` `Step`. The return value of the invocation is a, possibly refined, `ActionDefinition`. As configured action properties change it is not uncommon that the `inputDataShape` and `outputDataShape` properties change along with any action property value suggestions within the `enum` property of any `propertyDefinitionSteps`.

### Salesforce create or update object action example

Considering that the user has selected Salesforce connection and _create or update_ (`io.syndesis:salesforce-create-or-update:latest`) action, and now needs to specify the required parameters for that action: Salesforce object type (`sObjectName`) and Salesforce unique ID field (`sObjectIdName`). The UI needs to determine what screens need to be provided to the user.

```http
POST /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest
Content-Type: application/json
Accept: application/json
Length: 0

HTTP/1.1 200 OK
Content-Type: application/json
{
  "propertyDefinitionSteps": [{
    "name": "Salesforce object",
    "description": "Specify the Salesforce object to create or update",
    "properties": {
      "sObjectName": {
        "kind": "parameter",
        "displayName": "Salesforce object type",
        "group": "common",
        "required": false,
        "type": "string",
        "javaType": "java.lang.String",
        "tags":[],
        "deprecated": false,
        "secret": false,
        "componentProperty": false,
        "defaultValue": "",
        "description": "Name of the Salesforce object to create or update",
        "enum": [{
            "label": "Account",
            "value": "Account"
          }, {
            "label": "Contact",
            "value": "Contact"
          }, ...
        ]
      }
    }  
  }, {
    "name": "Unique field",
    "description": "Specify field to hold the identifying value",
    "properties": {
      "sObjectIdName": {
        "kind": "parameter",
        "displayName": "ID field",
        "group": "common",
        "required": false,
        "type": "string",
        "javaType": "java.lang.String",
        "tags":[],
        "deprecated": false,
        "secret": false,
        "componentProperty": false,
        "defaultValue": "",
        "description": "Salesforce object's unique field",
        "enum": []
      }
    }
  }],
  "outputDataShape": {
    "kind": "java",
    "type": "org.apache.camel.component.salesforce.api.dto.CreateSObjectResult"
  }
}
```

The UI proceeds with presenting the first step of action property configuration by displaying _Salesforce object type_ (`sObjectName`) parameter selection on _Salesforce object_ step with the suggested values from `sObjectName` `enum` property.

The user picks the _Contact_ Salesforce object to create or update, and UI proceeds to present the _ID field_ (`sObjectIdName`) parameter selection on _Unique field_ step. To determine possible values for the Salesforce object unique ID field, the following request is issued:

```http
POST /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest
Content-Type: application/json
Accept: application/json

{
  "sObjectName": "Contact"
}

HTTP/1.1 200 OK
Content-Type: application/json
{
  "propertyDefinitionSteps": [{
    "name": "Salesforce object",
    "description": "Specify the Salesforce object to create or update",
    "properties": {
      "sObjectName": {
        "kind": "parameter",
        "displayName": "Salesforce object type",
        "group": "common",
        "required": false,
        "type": "string",
        "javaType": "java.lang.String",
        "tags":[],
        "deprecated": false,
        "secret": false,
        "componentProperty": false,
        "defaultValue": "",
        "description": "Name of the Salesforce object to create or update",
        "enum": [{
            "label": "Account",
            "value": "Account"
          }, {
            "label": "Contact",
            "value": "Contact"
          }, ...
        ]
      }
    }  
  }, {
    "name": "Unique field",
    "description": "Specify field to hold the identifying value",
    "properties": {
      "sObjectIdName": {
        "kind": "parameter",
        "displayName": "ID field",
        "group": "common",
        "required": false,
        "type": "string",
        "javaType": "java.lang.String",
        "tags":[],
        "deprecated": false,
        "secret": false,
        "componentProperty": false,
        "defaultValue": "",
        "description": "Salesforce object's unique field",
        "enum": [{
            "displayValue": "Salesforce object identifier",
            "value": "Id"
          }, {
            "displayValue": "Twitter handle",
            "value": "TwitterScreenName__c"
          }, ...
        ]
      }
    }
  }],
  "inputDataShape": {
    "kind": "json",
    "type": "Contact",
    "spec": "{\"type\":\"object\",\"id\":\"urn:jsonschema:org:apache:camel:component:salesforce:dto:Contact\",\"title\":\"Contact\",\"properties\":{\"Id\":{\"type\":\"string\",\"required\":true,\"readonly\":true,\"description\":\"idLookup\",\"title\":\"Contact ID\"},\"IsDeleted\":{\"type\":\"boolean\",\"required\":true,\"readonly\":true,\"title\":\"Deleted\"},..."
  },
  "outputDataShape": {
    "kind": "java",
    "type": "org.apache.camel.component.salesforce.api.dto.CreateSObjectResult"
  }
}
```

The UI has passed the current property value pairs to the backend and the backend has determined that the prerequisite for _sObjectIdName_ has been specified, so it provides _sObjectIdName_ suggestions and the `inputDataShape` detail.

### SQL invoke stored procedure action example

Considering that the user has selected SQL connection and _invoke stored procedure_ (`io.syndesis:sql-invoke-stored-procedure:latest`) action, and now needs to specify the single required parameter for that action: SQL stored procedure name (`storedProcedure`).

```http
POST /api/v1/connections/2/actions/io.syndesis:sql-invoke-stored-procedure:latest
Content-Type: application/json
Accept: application/json
Length: 0

HTTP/1.1 200 OK
Content-Type: application/json
{
  "propertyDefinitionSteps": [{
    "name": "Stored procedure",
    "description": "The name of the stored procedure to invoke",
    "properties": {
      "storedProcedure": {
        "kind": "parameter",
        "displayName": "Stored procedure name",
        "group": "common",
        "required": false,
        "type": "string",
        "javaType": "java.lang.String",
        "tags":[],
        "deprecated": false,
        "secret": false,
        "componentProperty": false,
        "defaultValue": "",
        "description": "The name of the stored procedure to invoke",
        "enum": [
          {
            "label": "Create invoice stored procedure",
            "value": "INVOICE"
          },
          {
            "label": "Create quote stored procedure",
            "value": "QUOTE"
          },...
        ]
      }
    }  
  }],
  "outputDataShape": {
    "kind": "java",
    "type": "io.syndesis.connectors.sql.stored.Result"
  }
}
```
