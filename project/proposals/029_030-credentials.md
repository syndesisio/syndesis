# Credential support

* Issue: https://github.com/syndesisio/syndesis-project/issues/29 https://github.com/syndesisio/syndesis-project/issues/30
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

As a citizen user I would like to connect to 3rd party service that require me to authenticate using credentials. Credentials that I provide or that are received during the authorization with the 3rd party service would need to be stored in Syndesis so I can manage them. For example when connecting to Salesforce the I grant Syndesis rights to perform actions on my behalf by, and in order to do that I would like to choose "connect to Salesforce" in Syndesis authenticate against Salesforce to authorize that access and not be prompted again for authorization. Syndesis in turn would apply the credentials received in the authorization process with Salesforce to my Salesforce connection, and perform any management needed, such as refreshing the the received credentials.

My requirements are:

 * Simplicity in authorization flow ("connect to Salesforce" button)
 * Secure storage of authorization data (credentials)
 * Have Syndesis manage credential lifecycle

## Discussion

The idea of credential provider is to encapsulate managing specifics of 3rd party SaaS credentials.

Currently credentials are represented by connection properties that are marked as secret. This proposes an abstraction over existing connection properties with the ability to implement specific functionality for getting credentials such as OAuth tokens from 3rd party SaaS solutions. As such existing connection properties and their persistence as k8s secrets would remain the same, the UI would also have the choice of either managing the connection properties in the same way as it is currently done or can call into credentials provider implementation, specific to each SaaS solution, that would *apply* credentials to connection properties on the backend.
Each 3rd party SaaS can have slightly different OAuth flow which mandates the need for credential provider, a plug in, possibly shipped as a separate artifact and discovered at runtime.
Considering that OAuth requires per-user (OAuth token) and per-application secrets (Client Secret), there is a need for system (Syndesis owned) and user (citizen user owned) credentials. Melding of those would be also responsibility of credential provider.

Management of user and system credentials is also performed by credential provider, for instance initial setup of system credentials or refresh of user credentials (where applicable).


## User interaction

A citizen user during the creation of new connection selects `connect to _Service_`, this leads the user to the 3rd party service requiring the users credential to authorize Syndesis access on the users behalf. The user authenticates and authorizes Syndesis's access, and is returned to the same create connection screen which now shows that Syndesis is allowed to access the service.
The negative outcome is also possible if the user does not authorize Syndesis or if there is an error in the process, at that point user should be given an opportunity to retry the process.

## Domain

There are two types of domain objects, differentiated by security: public data for UI and sensitive secrets for the backend.

Credentials need to be combined or linked together with other credentials, this has merits when considering that OAuth credentials are partially owned by the user and partially owned by the system. For example Syndesis owns non-public credentials (OAuth Client Secret, X.509 certificate and private key) and semi-public (OAuth Client ID), that need to be used during acquisition and in a running integration together with private credentials associated with the user (username, OAuth authorization token, OAuth refresh token). This makes for two credentials with two different security and lifecycle requirements.

NOTE: Salesforce supports using x509 certificates for OAuth flow, and that should be preferred as the refreshing OAuth token can be performed within certificate's expiry i.e. not depending on the refresh token.

## REST API

To interact with the credentials API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| GET | /api/{version}/connectors/{connector}/credentials | List all the ways a credential can be acquired for a {connector} |
| POST | /api/{version}/connectors/{connector}/credentials | Acquire new credential for connection with identifier {id} |
| \* | /api/{version}/credentials/callback | Handle interaction with 3rd party services during credential acquisition |

## Persisting state

OAuth flow state that consists of:
 - connection id - identifies the connection for which the OAuth flow is performed
 - provider id - identifies the credential provider that will handle the OAuth flow
 - return URL - given by the UI, used to redirect the user in case of successful OAuth flow
 - OAuth state key - used to prevent replay attacks
 - OAuth token - needed for OAuth1 flow

The flow state is persisted in a HTTP cookie keyed by the id of connection. Tampering of the cookie is prevented by using RFC 6896 for Cookie processing.

### Examples

#### Acquisition example with Salesforce

Here is an example of credential workflow starting with acquisition for Salesforce:

![Alt text](https://g.gravizo.com/source/salesforce_example?https%3A%2F%2Fraw.githubusercontent.com%2Fsyndesisio%2Fsyndesis-project%2Fmaster%2Fproposals%2F003-credentials.md)
<details>
<summary></summary>
salesforce_example
@startuml
actor User
User -> "Syndesis UI": Create connection to Salesforce
"Syndesis UI" -> "Syndesis REST": (1) Determine credential metadata
activate "Syndesis REST"
"Syndesis REST" --> "Syndesis UI": metadata
deactivate "Syndesis REST"
User -> "Syndesis UI": Connect to salesforce
"Syndesis UI" -> "Syndesis REST": (2) Connect to Salesforce [returnUrl]
activate "Syndesis REST"
"Syndesis REST" --> "Syndesis UI": [redirect, Salesforce login URL]
deactivate "Syndesis REST"
"Syndesis UI" --> User: (3) Redirect to Salesforce login
User -> Salesforce: Authenticate and authorize Syndesis
activate Salesforce
Salesforce --> User: (4) Redirect to submit authorization codes
deactivate Salesforce
User -> "Syndesis REST": Submit authorization codes (redirect)
activate "Syndesis REST"
"Syndesis REST" -> Salesforce: (5) Request authorization
activate Salesforce
Salesforce --> "Syndesis REST": [Authorization tokens]
deactivate Salesforce
"Syndesis REST" -> "Syndesis REST": (6) Store authorization tokens
"Syndesis REST" --> User: Authorization successful
deactivate "Syndesis REST"
@enduml
salesforce_example
</details>

Details of the exchange:

 1. UI lists all methods of credential acquisition
```http
GET /api/v1/connectors/salesforce/credentials HTTP/1.1
Accept: application/json
```
```json
{
  "description": "salesforce",
  "icon": "salesforce",
  "label": "salesforce",
  "type": "OAUTH2"
}
```
 2. Citizen user chooses "Connect to Salesforce", this results in creation of new credential acquisition:
```http
POST /api/v1/connectors/salesforce/credentials HTTP/1.1
Content-Type: application/json
Accept: application/json
```
```json
{
  "returnUrl": "/ui#state"
}
```
```json
{
  "redirectUrl": "https://login.salesforce.com/services/oauth2/authorize?client_id=...&response_type=code&redirect_uri=...&scope&state=...",
  "type": "OAUTH2",
  "state": {
    "persist": "COOKIE",
    "spec": "cred-o2-...=...;Version=1;Path=/credentials/callback;Secure;HttpOnly"
  }
}
```
3. UI persists the state as requested in a HTTP cookie UI named `connection-__some_id__-oauth` with the given value and redirects user to the specified `url`, and Salesforce in turn redirects to OAuth callback:
```http
GET /services/oauth2/authorize?... HTTP/1.1
Host: login.salesforce.com
```
```http
HTTP/1.1 302 Moved Temporarily
Location: https://{syndesis-rest}/api/v1/credentials/callback?code=...
```
4. Credential provider for Salesforce processes the request, extracts the `code` and returns to the URL provided by the UI in 2. (`returnUrl`) and sets a new HTTP cookie that holds tokens or other data needed for application when the connection is eventually created
```http
GET /api/v1/connectors/salesforce/credentials/callback?code=... HTTP/1.1

Cookie: cred-o2-...=...
```
```http
HTTP/1.1 302 Moved Temporarily
Set-Cookie: cred-o2-...=...;Version=1;Path=/connections/;Secure;HttpOnly
Location: https://{syndesis-ui}/ui#state
```
5. Acquisition handler for Salesforce issues an out of bounds request to Salesforce that replies with the OAuth tokens
```http
POST /services/oauth2/token HTTP/1.1
Host: login.salesforce.com
```
```http
grant_type=authorization_code&client_id={client-id}&client_secret={client-secret}&redirect_uri=https://{syndesis-rest}/api/v1/connectors/salesforce/callback&code=...
```
6. This results in credential data being created for Salesforce connection
7. Eventually the UI will POST to `/api/v1/connections` to create a new Connection at what point the HTTP cookie set in 4. will be used to update the connection with credential data
