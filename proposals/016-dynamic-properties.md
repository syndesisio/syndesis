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
| POST      | /api/{version}/connections/{connectionId}/actions/{actionId}/parameter/options | Lists available property values for given chosen property values |

Swagger snippet:

```yaml
/connections/{id}/actions/{actionId}/parameter/options:
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

If the user is working with Salesforce Action _create or update_ Salesforce object and has already picked the _Contact_ Salesforce object to create or update and wishes to determine possible values for the Salesforce object unique ID field, the following request is issued:

```http
POST /api/v1/connections/2/actions/io.syndesis:salesforce-create-or-update:latest/parameter/options
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
