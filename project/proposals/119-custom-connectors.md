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

| HTTP Verb        | Path                                                                    | Description                                                                                                                                                                            |
| ---------------- | ----------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET              | /api/**{version}**/connector-templates                                  | Returns a list of known connector templates                                                                                                                                            |
| GET              | /api/**{version}**/connector-templates/**{templateId}**                 | Returns a specific connector template identified by the given **templateId**                                                                                                           |
| POST             | /api/**{version}**/connectors/custom/info                               | Provides information like proposed name, icon and description for new connector                                                                                                        |
| POST (multipart) | /api/**{version}**/connectors/custom/info                               | Provides information like proposed name, icon and description for new connector. Accepts `multipart/form-data` request with two fields: `connectorSettings` and `swaggerSpecification` |
| POST             | /api/**{version}**/connectors/custom                                    | Create a new custom connector                                                                                                                                                          |
| POST (multipart) | /api/**{version}**/connectors/custom                                    | Create a new custom connector. Accepts `multipart/form-data` request with two fields: `connectorSettings` and `icon`                                                                   |
| GET              | /api/**{version}**/connectors?query=connectorGroupId%3D**{templateId}** | Lists all connectors that were created from a template identified with **templateId**                                                                                                  |
| GET              | /api/**{version}**/connectors/**{connectorId}**                         | Fetches a connector with the id **{connectorId}**                                                                                                                                      |
| GET              | /api/**{version}**/connectors/**{connectorId}**/details                 | Fetches details in same form as the `/custom/info` endpoint above                                                                                                                      |
| DELETE           | /api/**{version}**/connectors/**{connectorId}**                         | Deletes a connector with the id **{connectorId}**                                                                                                                                      |
| PUT              | /api/**{version}**/connectors/**{connectorId}**                         | Updates a connector with the id **{connectorId}**                                                                                                                                      |
| PUT (multipart)  | /api/**{version}**/connectors/custom                                    | Updates a connector with the id **{connectorId}**. Accepts `multipart/form-data` request with two fields: `connector` and `icon`                                                       |
| GET              | /api/**{version}**/connectors/**{connectorId}**/icon                    | Serves the connector icon if one is saved separately in the DB                                                                                                                         |
| POST (multipart) | /api/**{version}**/connectors/**{connectorId}**/icon                    | Updates the icon for the specified connector. Accepts `multipart/form-data` request with one field: `icon`                                                                             |

### New custom connector based on Swagger specification example

Given that the Syndesis database contains a connector template, presented here
with some properties omitted in the form expected by :

```json
{
  "kind": "connector-template",
  "data": {
    "id": "swagger-connector-template",
    "name": "Swagger API Connector",
    "description": "Swagger API Connector",
    "icon": "fa-link",
    "camelConnectorGAV": "io.syndesis:rest-swagger-connector:@syndesis.version@",
    "camelConnectorPrefix": "swagger-operation",
    "connectorGroup" : {
      "id": "swagger-connector-template" // inherited by all custom connectors generated from this template as `connectorGroup` and `connectorGroupId`
    },
    ...
    "properties": {
      "specification": {
        "kind": "property",
        "displayName": "Specification file",
        "required": true,
        "type": "string",
        "tags": [ "blob" ],
        "description": "Upload the Swagger specification",
        ...
      },
      "host": {
        "kind": "property",
        "displayName": "Host",
        "required": false,
        "type": "string",
        "description": "Scheme hostname and port..."
      },
      "basePath": {
        "kind": "property",
        "displayName": "Base path",
        "required": false,
        "type": "string",
        "description": "API basePath for example /v2..."
      },
      //...
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
GET /api/v1/connector-templates/swagger-connector-template HTTP/1.1
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 1895

{
  // the connector template presented above in the `data` property
  //...
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
  //...
}
```

Based on the connector template property `specification`, tagged with `upload`,
`url` as hints to on how to present the form item, from the above response the
UI can offer the user to upload or provide a URL to the specification.

The user opts to specify the Swagger specification via URL of the specification,
but mistakenly selects the URL of the HTML document instead of the
specification, the UI can perform validation before proceeding by invoking:

```http
POST /api/v1/connectors/custom/info HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "connectorTemplateId": "swagger-connector-template"
  "configuredProperties": {
    "specification": "http://petstore.swagger.io/index.html"
  }
}

HTTP/1.1 200 OK

{
  "errors": [
    {
      "error": "error",
      "message": "Unable to read Swagger specification from: http://petstore.swagger.io/index.html",
      "property": ""
    }
  ],
  "warnings": [],
  "properties": {}
}
```

The user provides the correct URL, now the response is positive and information
fetched from the specification is returned:

```http
POST /api/v1/connectors/custom/info HTTP/1.1
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
  // information needed for the 'Review Swagger Actions' step
  "actionsSummary": {
    "actionCountByTags": {
      "store": 4,
      "user": 8,
      "pet": 8
    },
    "totalActions": 20
  },

  // suggestion for the 'Description' input on the 'General Connector Info' step
  "description": "Petstore API connector",

  // no errors, yay!
  "errors": [],

  // one warning
  "warnings": [
    {
      "error": "unsupported-auth",
      "message": "Authentication type apiKey is currently not supported",
      "property": ""
    }
  ],

  // suggestion for the 'Name' input on the 'General Connector Info' step
  "name": "Petstore",

  // set of properties that are used on 'Security' and 'General Connector Info' steps
  "properties": {

    // part of the connector-template, so it's included here, not used by the UI
    // will be used when connection is generated from the newly created connector
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

    // suggestion for the 'Token Endpoint' (OAuth2 Token endpoint) no 'defaultValue' property here nothing can be suggested
    "tokenEndpoint": {
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

    // supported authentication types, influences the selection presented on the 'Security' step
    // based on the 'enum' property: the values present specify what can be offered
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

    // suggestion for the 'Authorization Endpoint' (OAuth2 Authorization endpoint), 'defaultValue' is the value suggested
    // the UI should prefil the input with: http://petstore.swagger.io/oauth/dialog
    "authorizationEndpoint": {
      "componentProperty": true,
      "defaultValue": "http://petstore.swagger.io/oauth/dialog",
      "deprecated": false,
      "description": "URL for the start of the OAuth flow",
      "displayName": "OAuth Authorization URL",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": true,
      "secret": false,
      "type": "string",
      "tags": [
        "oauth-authorization-url"
      ],
      "enum": []
    },

    // suggestion for the 'Base URL' (API Base Path), 'defaultValue' is the value suggested
    // the UI should prefil the input with: /v2
    "basePath": {
      "componentProperty": true,
      "defaultValue": "/v2",
      "deprecated": false,
      "description": "API basePath for example /v2. Default is unset if set overrides the value present in Swagger specification.",
      "displayName": "Base path",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "string",
      "tags": [],
      "enum": []
    },

    // part of the connector-template, so it's included here, not used by the UI
    // will be used when connection is generated from the newly created connector
    "clientId": {
      "componentProperty": true,
      "deprecated": false,
      "description": "OAuth Client ID, sometimes called Consumer Key",
      "displayName": "OAuth Client ID",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "string",
      "tags": [
        "oauth-client-id"
      ],
      "enum": []
    },

    // part of the connector-template, so it's included here, not used by the UI
    // will be used when connection is generated from the newly created connector
    "clientSecret": {
      "componentProperty": true,
      "deprecated": false,
      "description": "OAuth Client Secret, sometimes called Consumer Secret",
      "displayName": "OAuth Client Secret",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": true,
      "type": "string",
      "tags": [
        "oauth-client-secret"
      ],
      "enum": []
    },

    // suggestion for the 'Host' (API Scheme and hostname), 'defaultValue' is the value suggested
    // the UI should prefil the input with: http://petstore.swagger.io
    "host": {
      "componentProperty": true,
      "defaultValue": "http://petstore.swagger.io",
      "deprecated": false,
      "description": "Scheme hostname and port to direct the HTTP requests to in the form of https://hostname:port. Can be configured at the endpoint component or in the corresponding REST configuration in the Camel Context. If you give this component a name (e.g. petstore) that REST configuration is consulted first rest-swagger next and global configuration last. If set overrides any value found in the Swagger specification RestConfiguration. Can be overridden in endpoint configuration.",
      "displayName": "Host",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "string",
      "tags": [],
      "enum": []
    },

    // part of the connector-template, so it's included here, not used by the UI
    // will be used in the integration when action is added to integration step
    "operationId": {
      "componentProperty": false,
      "deprecated": false,
      "description": "ID of operation to invoke",
      "displayName": "Operation ID",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "hidden",
      "tags": [],
      "enum": []
    },

    // part of the connector-template, so it's included here, not used by the UI
    // used at runtime
    "specification": {
      "componentProperty": true,
      "deprecated": false,
      "description": "Swagger specification of the service",
      "displayName": "Specification",
      "group": "producer",
      "javaType": "java.lang.String",
      "kind": "property",
      "label": "producer",
      "required": false,
      "secret": false,
      "type": "hidden",
      "tags": [
        "upload",
        "url"
      ],
      "enum": []
    }
  }
}
```

With that the user can opt to change some of the data, here the user opted to
change the name of the new connector from the suggested _"Swagger Petstore"_ to
_"Petstore API"_, and the new connector can be created:

```http
POST /api/v1/connectors/custom HTTP/1.1
Content-Type: application/json
Accept: application/json

{
  "connectorTemplateId": "swagger-connector-template",
  "name": "Petstore API", // 'Connector Name' from 'General Connector Info' step
  "description": "This is...", // 'Description' from 'General Connector Info' step
  "icon": "data:image/svg+xml;utf8,<svg ...", // TODO
  "configuredProperties": {
    "specification": "http://petstore.swagger.io/v2/swagger.yaml", // Specification from 'Upload Swagger' step
    "host": "http://petstore.swagger.io", // 'Host' from 'General Connector Info' step
    "basePath": "/v2", // 'Base URL' from 'General Connector Info' step
    "authenticationType": "oauth2", // the 'value' from the 'enum' of 'authenticationType' property beget from /info endpoint
    "authorizationEndpoint": "http://petstore.swagger.io/oauth/dialog", // 'Authorization Endpoint URL' from 'General Connector Info' step
    "tokenEndpoint": "http://petstore.swagger.io/oauth/token" // 'Token Endpoint URL' from 'General Connector Info' step
  }
}

HTTP/1.1 200 OK
Content-Type: application/json

{
  ... // connector data
}
```

### Fetching data about custom connectors

To list custom connectors created from a connector template with id
`swagger-connector-template` issue a request like:

```http
GET /api/v1/connectors/custom?templateId=swagger-connector-template HTTP/1.1
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json

{
    "items": [
      // connectors
    ],
    total: // number of connectors
}
```
