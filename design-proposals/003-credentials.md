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

As a citizen user I would like to connect to 3rd party service that require me to authenticate using credentials. Credentials that I provide or that are received during the authorization with the 3rd party service would need to be stored in Syndesis so I can manage them. For example when connecting to Salesforce the I grant Syndesis rights to perform actions on my behalf by, and in order to do that I would like to choose "connect to Salesforce" in Syndesis authenticate against Salesforce to authorize that access and not be prompted again for authorization. Syndesis in turn would apply the credentials received in the authorization process with Salesforce to my Salesforce connection, and perform any management needed, such as refreshing the the received credentials. I would like to have the ability to manage credentials that are stored in Syndesis.

My requirements are:

 * Simplicity in authorization flow ("connect to Salesforce" button)
 * Secure storage of authorization data (credentials)
 * Ability to manage credentials: delete and update
 * Ability to apply credentials to connections

## Discussion 

The idea of credential provider is to encapsulate managing specifics of 3rd party SaaS credentials.
Currently credentials are represented by connection properties that are marked as secret. This proposes an abstraction over existing connection properties with the ability to implement specific functionality for getting credentials such as OAuth tokens from 3rd party SaaS solutions. As such existing connection properties and their persistance as k8s secrets would remain the same, the UI would also have the choice of either managing the connection properties in the same way as it is currently done or can call into credentials provider implementation, specific to each SaaS solution, that would *apply* credentials to connection properties on the backend.
Each 3rd party SaaS can have slightly different OAuth flow which mandates the need for credential provider, a plug in, possibly shipped as a separate artifact and discovered at runtime, mounted at `/api/{version}/credentials/{connector}` to facilitate the OAuth flow and manage (i.e. *apply*) the resulting credentials.
Considering that OAuth requires per-user and per-application secrets, there is a need for system (Syndesis owned) and user (citizen user owned) credentials. Melding of those would be also responsibility of credential provider.
Management of user and system credentials is also performed by credential provider, for instance initial setup of system credentials or refresh of user credentials.
Credential provider consists of:
 * **handler** - to implement OAuth flow (REST)
 * **applicator** - to apply credentials to connection properties
 * **deployer** - to augment k8s deployment descriptor
 * **manager** - to manage credentials

## Domain

There are two types of domain objects, differentiated by security: public data for UI and sensitive secrets for the backend.

Credentials need to be combined or linked together with other credentialsm, this has merits when considering that OAuth credentials are partialy owned by the user and partialy owned by the system. For example Syndesis owns non-public credentials (OAuth Client Secret, X.509 certificate and private key) and semi-public (OAuth Client ID), that need to be used during acquisition and in a running integration together with private credentials associated with the user (username, OAuth authorization token, OAuth refresh token). This makes for two credentials with two different security and lifecycle requirements.

NOTE: Salesforce supports using x509 certificates for OAuth flow, and that should be prefered as the refreshing OAuth token can be performed within certificate's expiry i.e. not depending on the refresh token.

### Credential view model

Each credential should have a name so that the user can identify it, type: this can be username-password, x509 or OAuth tokens, internal identifier and connector label so it can be determined to what connection it can be applied to.

The credential name can be auto generated (e.g. "Salesforce credentials for user@example.com").

View model credential properties:

 * **id**        : System assigned identifier for the credential, e.g. UUID or sequence
 * **name**      : Name of the credential, for humans, e.g. "Salesforce credentials for user@example.com"
 * **connector** : Conector type (label) that the credential applies to, e.g. "salesforce"
 * **created**   : Date/time the credential was created
 * **note**      : Optional note, could be generated during credential acquisition, e.g. "Expires 23.11.2018"
 
This constititutes the view model, notice that the sensitive (secret) data is not exposed to the UI -- the UI should reference credentials by the identifier.

### Credential backend model

Credential backend model is key-value pairs grouped together under the same identifier. The identifier is identical in value to the identifier used in the view model. Keys of the pairs correspond to the property names needed for the Spring Boot configuration of the Camel components.

## REST API

To interact with the credentials API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| GET | /api/{version}/credentials/{connector} | List all the ways a credential can be acquired for a {connector} |
| POST | /api/{version}/credentials/{connector} | Acquire new credential for connector with label {connector} |
| \* | /api/{version}/credentials/{connector}/acquire/\*\* | Handle interaction with 3rd party services during credential acquisition |
| GET | /api/{version}/credentials | List all credentials created by the current tennant |
| DELETE | /api/{version}/credentials/{connector}/{id} | Remove credential from the system |

### Examples

#### Acquisition example

Here is an example of credential workflow starting with acquisition for Salesforce:
 1. UI lists all methods of credential acquisition
```http
GET /api/v1/credentials/salesforce HTTP/1.1
Accept: application/json
```
```json
{
  "response": {
    "methods": [{
      "type": "jwt-oauth",
      "label": "Connect to Salesforce",
      "icon": "x-salesforce",
      "description": "You will be redirected to Salesforce to authorize OAuth usage."
    }]
  }
}
```
 2. Citizen user chooses "Connect to Salesforce", this results in creation of new credential acquisition:
```http
POST /api/v1/credentials/salesforce HTTP/1.1
Content-Type: application/json
Accept: application/json
```
```json
{
  "input": {
    "method": "jwt-oauth",
    "returnUrl": "https://{syndesis-ui}/..."
  }
}
```
```json
{
  "response": {
    "action": "redirect",
    "url": "https://login.salesforce.com/services/oauth2/authorize?..."
  }
}
```
3. UI redirects user to the specified `url`, and Salesforce in turn redirects to OAuth callback:
```http
GET /services/oauth2/authorize?... HTTP/1.1
Host: login.salesforce.com
```
```http
HTTP/1.1 302 Moved Temporarily
Location: https://{{syndesis-rest}/api/v1/credentials/salesforce/acquire?code=...
```
4. Acquisition handler for Salesforce handles the request, extracts the `code` and returns to the URL provided by the UI in 2. (`returnUrl`)
```http
GET /api/v1/credentials/salesforce/acquire?code=... HTTP/1.1
```
```http
HTTP/1.1 302 Moved Temporarily
Location: https://{syndesis-ui}/...
```
5. Acquisition handler for Salesforce issues an out of bounds request to Salesforce that replies with the OAuth tokens
```http
POST /services/oauth2/token HTTP/1.1
Host: login.salesforce.com
```
```http
grant_type=authorization_code&client_id={sindesis OAuth client id}&client_secret={syndesisOauth client secret}&redirect_uri={syndesis ui returnUrl}&code=...
```
6. This results in credential being created for Salesforce connection

## Misc / Open Points
 
 * Choice of persistence for security sensitive properties (assuming k8s secrets)
 * If using k8s secrets, does each tennant get it's own service account with the rights to manage secrets (read-write)
 * If using k8s secrets, do integration pods get their own service account so they (alone) can access secrets (read-only)
 

