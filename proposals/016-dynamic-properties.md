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

`properties` map is replaced with `ActionMetadata`:
```java
public interface Action extends WithId<Action>, WithName, Serializable {
  //...
  ActionMetadata metadata();
  //...
}
```

`ActionMetadata` is introduced to contain the `Step`s:
```java
public interface ActionMetadata {

    interface Step extends WithName, WithProperties {

        String description();

    }

    List<Step> propertyDefinitionSteps();

}
```

For metadata value proposition the domain builds upon the _key-value_ model of properties that can be specified used to parameterize Actions. For a given property _key_ zero or more _values_ are provided based on the currently known context defined by connector, connection and already defined action property values.

## API

To interact with the dynamic parameter API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| GET       | /api/{version}/connections/{connectionId}/actions/{actionId}/metadata        | Fetches the action metadata for given action id |
| GET       | /api/{version}/connections/{connectionId}/actions/{actionId}/metadata/values | Provides value suggestions for chosen values    |

### Salesforce create or update object action example

Considering that the user has selected Salesforce connection and _create or update_ (`io.syndesis:salesforce-create-or-update:latest`) action, and now needs to specify the required parameters for that action: Salesforce object type (`sObjectName`) and Salesforce unique ID field (`sObjectIdName`). The UI needs to determine what screens need to be provided to the user.

```http
GET /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/metadata

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
        "description": "Name of the Salesforce object to create or update"
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
        "description": "Salesforce object's unique field"
      }
    }
  }]
}
```

The UI proceeds with presenting the first step of action property configuration by displaying _Salesforce object type_ (`sObjectName`) parameter selection on _Salesforce object_ step. At this point there are no parameter values the user has specified, so the UI tries to determine parameter values suggestions requesting without any query parameters:

```http
GET /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/metadata/values

HTTP/1.1 200 OK
Content-Type: application/json

{
  "sObjectName": [
    {
      "displayValue": "Account",
      "value": "Account"
    },
    {
      "displayValue": "Contact",
      "value": "Contact"
    },...
  ]
}

```

The backend by the specified action determines that the prerequisite for `sObjectIdName` parameter has not been specified (no `sObjectName` given), and returns only the value suggestions for parameters without prerequisites - in this case `sObjectName`.

The user picks the _Contact_ Salesforce object to create or update, and UI proceeds to present the _ID field_ (`sObjectIdName`) parameter selection on _Unique field_ step. To determine possible values for the Salesforce object unique ID field, the following request is issued:

```http
GET /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/metadata/values?sObjectName=Contact

HTTP/1.1 200 OK
Content-Type: application/json

{
  "sObjectIdName": [
    {
      "displayValue": "Salesforce object identifier",
      "value": "Id"
    },
    {
      "displayValue": "Twitter handle",
      "value": "TwitterScreenName__c"
    }
  ]
}
```

The UI has passed the current property value pairs to the backend and the backend has determined that the prerequisite for _sObjectIdName_ has been specifed, so it provides _sObjectIdName_ suggestions.

### SQL invoke stored procedure action example

Considering that the user has selected SQL connection and _invoke stored procedure_ (`io.syndesis:sql-invoke-stored-procedure:latest`) action, and now needs to specify the single required parameter for that action: SQL stored procedure name (`storedProcedure`).

At this point there are no parameter values the user has specified, so the UI tries to determine parameter values suggestions by without query parameters:

```http
GET /api/v1/connections/2/actions/io.syndesis:sql-invoke-stored-procedure:latest/metadata/values

HTTP/1.1 200 OK
Content-Type: application/json

{
  "storedProcedure": [
    {
      "displayValue": "Create invoice stored procedure",
      "value": "INVOICE"
    },
    {
      "displayValue": "Create quote stored procedure",
      "value": "QUOTE"
    },...
  ]
}
```
