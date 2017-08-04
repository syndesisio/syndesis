# Support for dynamic properties

* Issue: https://github.com/syndesisio/syndesis-project/issues/16
* Sprint: 15
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

## Background

For some connectors it's difficult or cumbersome for users to specify property values, for instance there could be many property values that user can choose from (e.g. Salesforce Object types) or without offering possible values user has a hard time determining what would be the best property value to input (e.g. what unique field to use with create or update action with Salesforce). Having a fixed predefined set of property values is not feasible as they can differ between users, organizations or versions, i.e. they must be determined at runtime.

This functionality will give support to UI for implementing a pick list, auto-complete or property filter functionality. The initial implementation will focus on Salesforce use case, but it should provide enough room for other use cases.

## User Story

As a citizen user, I would like to pick property values instead of or in addition to manually inputting them in order to make it easier to select appropriate values or make fewer mistakes when selecting particular value in particular context.
For example when using Salesforce's connector action _create or update_ I need to select a _Salesforce object type_ I wish to create or update and a _unique field_ of that type used to check for duplicates.

## Domain

The domain builds upon the _key-value_ model of properties that can be specified used to parameterize Actions. For a given property _key_ zero or more _values_ are provided based on the currently known context defined by connector, connection and already defined action property values.

## API

To interact with the dynamic parameter API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| POST      | /api/{version}/connections/{connectionId}/actions/{actionId}/parameters | Lists available property values for given chosen property values |

Swagger snippet:

```yaml
/connections/{id}/actions/{actionId}/parameters:
  post:
    tags:
    - "connections"
    summary: "Lists available property values"
    description: "Returns a key - value-list of properties that can be specified\
      \ for already configured properties."
    operationId: "getParameterOptions"
    consumes:
    - "application/json"
    produces:
    - "application/json"
    parameters:
    - name: "id"
      in: "path"
      required: true
      type: "string"
      x-example: "my-salesforce-connection-id"
    - name: "actionId"
      in: "path"
      required: true
      type: "string"
      x-example: "io.syndesis:salesforce-create-or-update:latest"
    - in: "body"
      name: "configuredProperties"
      required: false
      schema:
        type: "object"
        additionalProperties:
          type: "string"
      x-examples:
        application/json: "{\"sObjectName\": \"Account\"}"
    responses:
      200:
        description: "Set of property key-value pairs that can be specified. The\
          \ list of values is filtered by the given `configuredProperties` to contain\
          \ only the values that make sense for those."
        schema:
          type: "object"
          additionalProperties:
            $ref: "#/definitions/ActionParameterValue"
```

### Example

Considering that the user has selected Salesforce connection and _create or update_ (`io.syndesis:salesforce-create-or-update:latest`) action, and now needs to specify the required parameters for that action: Salesforce object type (`sObjectName`) and Salesforce unique ID field (`sObjectIdName`).

At this point there are no parameter values the user has specified, so the UI tries to determine parameter values suggestions by posting an empty JSON object (`{}`):

```http
POST /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/parameters
Content-Type: application/json

{
}

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

The backend by the specified action determines that the prerequisite for _sObjectIdName_ parameter has not been specified (no _sObjectName_ given), and returns only the value suggestions for parameters without prerequisites - in this case _sObjectName_.

The user picks the _Contact_ Salesforce object to create or update and wishes to determine possible values for the Salesforce object unique ID field, the following request is issued:

```http
POST /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/parameters
Content-Type: application/json

{
  "sObjectName":"Contact"
}

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
