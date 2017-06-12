# Credential support

* Issue: https://github.com/syndesisio/syndesis-rest/issues/386
* Sprint: 13
* Affected Repos:
  - syndesis-rest
  - syndesis-ui
  
## Background

High level goals are:

 * Help users provide and manage credentials
 * Persist credentials in a secure manner
 * Apply credentials to connections with minimum impact on the existing implementation

## User Story

As a citizen user I would like to connect to 3rd party service that require authenticate using credentials. Credentials would need to be stored in Syndesis so I can manage and assign them to other integrations using same 3rd party services. For example when connecting to Salesforce the I need to provide username/password authentication only once to authorize Syndesis's access using OAuth, and in order to do that I would like to choose "connect to Salesforce" in Syndesis and not be prompted again for authorization.

My requirements are:

 * Simplicity in authorization flow ("connect to Salesforce" button)
 * Secure storage of authorization data (credentials)
 * Ability to manage credentials: delete and update
 * Ability to associate, or apply, credentials to connections

## Domain

There are two types of domain objects, differentiated by security: public data for UI and sensitive secrets for the backend.

### Credential view model

Each credential should have a name so that the user can identify it, type: this can be username-password, x509 or OAuth tokens, internal identifier and connector label so it can be determined to what connection it can be applied to.

The credential name can be auto generated (e.g. "Salesforce credentials for user@example.com").

There should be an option to combine two or more credentials into one credential, this has merits when considering that OAuth credentials are partialy owned by the user and partialy owned by the system. For instance when authenticating against Salesforce Client ID and Client Secret is combined with OAuth token and OAuth refresh token.

NOTE: Salesforce supports using x509 certificates for OAuth flow, and that should be prefered as the refreshing OAuth token can be performed within certificate's expiry i.e. not depending on the refresh token.

View model credential properties:

 * **id**        : System assigned identifier for the credential
 * **name**      : Name of the credential, for humans
 * **connector** : Conector type (label) that the credential applies to
 * **contains**  : Optional list of other credentials when it's to be used as group
 * **created**   : Date/time the credential was created
 * **note**      : Optional note, could be generated during credential acquisition
 
This constititutes the view model, notice that the sensitive (secret) data is not exposed to the UI -- the UI should reference credentials by the identifier.

### Credential backend model

Credential backend model is key-value pairs grouped together under the same identifier. The identifier is identical in value to the identifier used in the view model. Keys of the pairs correspond to the property names needed for the Spring Boot configuration of the Camel components.

## REST API

To interact with the credentials API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| POST | /api/{version}/credentials/{connector} | Acquire new credential for connector with label {connector} |
| \* | /api/{version}/credentials/{connector}/\*\* | Handle interaction with 3rd party services during credential acquisition |
| GET | /api/{version}/credentials | List all credentials created by the current tennant |
| GET | /api/{version}/credentials/{connector} | List all credentials that can be applied to connector with label {connector} and are created by the current tennant |
| DELETE | /api/{version}/credentials/{connector}/{id} | Remove credential from the system |
| PUT | /api/{version}/credentials/{connector}/{id} | Update credential in the system (where applicable) |

### Example

Salesforce credential acquisition is attempted by the user:



## Misc / Open Points
 
 * Choice of persistence for security sensitive properties (assuming k8s secrets)
 * If using k8s secrets, does each tennant get it's own service account with the rights to manage secrets (read-write)
 * If using k8s secrets, do integration pods get their own service account so they (alone) can access secrets (read-only)
 
