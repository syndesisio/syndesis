# Custom connectors

* Issue: https://github.com/syndesisio/syndesis-project/issues/119
* Sprint: 20

## Background

Allow expert integrators define custom connectors providing a set of properties
defined by the connector template and connector generator implementation that
uses the specified properties to create a new connector definition. This does
not create new Camel connector implementation, but relies on existing Camel
connectors.

An example of such custom connector is the REST API connector backed by a
Swagger specification. Rather than specifying a number of parameters at the
action or connection level, user can opt to create a custom connector from the
Swagger specification and than create multiple connections (if needed)
specifying typical connection parameters like authentication and have to define
only the action parameters specific to a particular REST endpoint.

## User Story

See https://github.com/syndesisio/syndesis-project/issues/173

## Data flow outline

Defining custom connectors should follow the same steps regardless of the
backing connector technology (Camel connector implementation).

First step is to provide values for the properties defined by the connector
template, for example for a custom Swagger connector: a Swagger specification.

Next these parameters are validated, and response that includes an optional list
of errors/warnings, `icon`, `name` and `description` and any additional
connector properties as suggestions are returned.

Finally a payload defining the new connector is submitted containing the
properties defined by the connector template, `icon`, `name` and `description`
properties.

## API

New API endpoints for defining custom connectors:

| HTTP Verb | Path                                            | Description                                                                     |
| --------- | ----------------------------------------------- | ------------------------------------------------------------------------------- |
| GET       | /api/**{version}**/connector-templates          | Returns a list of known connector templates                                     |
| GET       | /api/**{version}**/connector-templates/**{id}** | Returns a specific connector template identified by the given **id**            |
| POST      | /api/**{version}**/connectors/custom/info       | Provides information like proposed name, icon and description for new connector |
| POST      | /api/**{version}**/connectors/custom/           | Create a new custom connector                                                   |

### New custom connector based on Swagger specification example

Given that the Syndesis database contains a connector template, presented here
with some properties omitted:

```json
{
  "kind": "connector-template",
  "data": {
    "id": "swagger-connector-template",
    "name": "Swagger API Connector",
    "description": "Swagger API Connector",
    "icon": "fa-link",
    ...
    "properties": {
      "specification": {
        "kind": "property",
        "displayName": "Specification file",
        "required": true,
        "type": "string",
        "javaType": "java.lang.String",
        "tags": [ "blob" ],
        "description": "Upload the Swagger specification",
        ...
      },
      "specificationUrl": {
        "kind": "property",
        "displayName": "Specification URL",
        "required": true,
        "type": "string",
        "javaType": "java.lang.String",
        "description": "Provide the URL for the Swagger specification",
        ...
      }
    },
    "connectorProperties": {
      "host":{
        ...
      },
      "operationId": {
        ...
      },
      "specification": {
        ...
      }
    }
  }
}
```

The REST API supports multiple connector templates, we'll be focusing on the
connector template with the id `swagger-connector-template` as this is the first
use case we support.

UI fetches the definition of the connector template by using the
`swagger-connector-template` identifier:

```http
GET /api/v1/connector-templates/swagger-connector-template
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 1895

{...
  "properties": {
    "specification": {
      "kind": "property",
      "displayName": "Specification",
      "group": "producer",
      "label": "producer",
      "required": true,
      "type": "file",
      "javaType": "java.lang.String",
      "tags": [ "upload", "url" ],
      "deprecated": false,
      "secret": false,
      "componentProperty": true,
      "description": "Upload the swagger fore for your Custom API Client Connector. Custom API's are RESTful APIs and can be hosted anywhere, as long as a well-documented swagger is available and conforms to OpenAPI standards."
    }
  },
...} // the connector template presented above
```

Based on the connector template property `specification`, tagged with `upload`,
`url` as hints to on how to present the form item, from the above response the
UI can offer the user to upload or provide a URL to the specification.

The user opts to specify the Swagger specification via URL of the specification,
but mistakenly selects the URL of the HTML document instead of the
specification, the UI can perform validation before proceeding by invoking:

```http
POST /api/v1/connectors/custom/info
Content-Type: application/json
Accept: application/json

{
  "connectorTemplateId": "swagger-connector-template"
}

HTTP/1.1 400 Bad Request

{
  "errors" : [
    {
      "property": "specification",
      "error": "ValidSpecification",
      "message": "Given specification is not readable"
    }
  ]
}
```

The user provides the correct URL, now the response is positive and information
fetched from the specification is returned:

```http
POST /api/v1/connectors/custom/info
Content-Type: application/json
Accept: application/json

{
  "connectorTemplateId": "swagger-connector-template",
  "configuredProperties": {
    "specification": "http://petstore.swagger.io/v2/swagger.json"
  }
}

HTTP/1.1 200 OK

{
  "name": "Petstore",
  "description": "Petstore API connector",
  "properties": {
    "accessToken": {
      "componentProperty": true,
      "deprecated": false,
      "description": "OAuth Access token",
      "displayName": "OAuth Access token",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": true,
      "type": "string",
      "tags": [
        "oauth-access-token"
      ],
      "enum": []
    },
    "accessTokenUrl": {
      "componentProperty": true,
      "deprecated": false,
      "description": "URL to fetch the OAuth Access token",
      "displayName": "OAuth Access token URL",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "string",
      "tags": [
        "oauth-access-token-url"
      ],
      "enum": []
    },
    "authenticationType": {
      "componentProperty": true,
      "defaultValue": "oauth2",
      "deprecated": false,
      "description": "Type of authentication used to connect to the API",
      "displayName": "Authentication Type",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "string",
      "tags": [
        "authentication-type"
      ],
      "enum": [
        {
          "label": "OAuth 2.0",
          "value": "oauth2"
        }
      ]
    },
    "basePath": {
        //...
    },
    "clientId": {
        //...
    },
    "clientSecret": {
        //...
    },
    "host": {
        //...
    },
    "operationId": {
        //...
    },
    "specification": {
        //...
    }
  },
  "actionsSummary": {
    "actionCountByTags": {
      "store": 4,
      "user": 8,
      "pet": 8
    },
    "totalActions": 20
  },
  "warnings": [
  ],
  "errors": [
  ]
}
```

With that the user can opt to change some of the data, here the user opted to
change the name of the new connector from the suggested _"Swagger Petstore"_ to
_"Petstore API"_, and the new connector can be created:

```http
POST /api/v1/connectors/custom
Content-Type: application/json
Accept: application/json

{
  "connectorTemplateId": "swagger-connector-template",
  "name": "Petstore API",
  "description": "This is a sample server Petstore server. You can find out more about Swagger at http://swagger.io or on irc.freenode.net, #swagger. For this sample, you can use the api key special-key to test the authorization filters.",
  "icon": "data:image/svg+xml;utf8,<svg ...",
  "configuredProperties": {
    "specification": "http://petstore.swagger.io/v2/swagger.yaml",
    "host": "http://petstore.swagger.io",
    "basePath": "/v2",
    "authentication": {
      "type": "oauth2",
      "authorizationUrl": "http://petstore.swagger.io/oauth/dialog"
    }
  }
}

HTTP/1.1 200 OK
Content-Type: application/json

{
  ... // connector data
}
```
