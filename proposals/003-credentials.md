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

As a citizen user I would like to connect to 3rd party service that require me to authenticate using credentials. Credentials that I provide or that are received during the authorization with the 3rd party service would need to be stored in Syndesis so I can manage them. For example when connecting to Salesforce the I grant Syndesis rights to perform actions on my behalf by, and in order to do that I would like to choose "connect to Salesforce" in Syndesis authenticate against Salesforce to authorize that access and not be prompted again for authorization. Syndesis in turn would apply the credentials received in the authorization process with Salesforce to my Salesforce connection, and perform any management needed, such as refreshing the the received credentials.

My requirements are:

 * Simplicity in authorization flow ("connect to Salesforce" button)
 * Secure storage of authorization data (credentials)
 * Have Syndesis manage credential lifecycle

## Discussion 

The idea of credential provider is to encapsulate managing specifics of 3rd party SaaS credentials.

Currently credentials are represented by connection properties that are marked as secret. This proposes an abstraction over existing connection properties with the ability to implement specific functionality for getting credentials such as OAuth tokens from 3rd party SaaS solutions. As such existing connection properties and their persistence as k8s secrets would remain the same, the UI would also have the choice of either managing the connection properties in the same way as it is currently done or can call into credentials provider implementation, specific to each SaaS solution, that would *apply* credentials to connection properties on the backend.
Each 3rd party SaaS can have slightly different OAuth flow which mandates the need for credential provider, a plug in, possibly shipped as a separate artifact and discovered at runtime, mounted at `/api/{version}/credentials/{connector}` to facilitate the OAuth flow and manage (i.e. *apply*) the resulting credentials.
Considering that OAuth requires per-user and per-application secrets, there is a need for system (Syndesis owned) and user (citizen user owned) credentials. Melding of those would be also responsibility of credential provider.

Management of user and system credentials is also performed by credential provider, for instance initial setup of system credentials or refresh of user credentials.


## User interaction

A citizen user during the creation of new connection selects `conect to _Service_`, this leads the user to the 3rd party service requiring the users credential to authorise Syndesis access on the users behalf. The user authenticates and authorizes Syndesis's access, and is returned to the same create connection screen which now shows that Syndesis is allowed to access the service.
The negative outcome is also possible if the user does not authorize Syndesis or if there is an error in the process, at that point user should be given an opportunity to retry the process.

## Domain

There are two types of domain objects, differentiated by security: public data for UI and sensitive secrets for the backend.

Credentials need to be combined or linked together with other credentials, this has merits when considering that OAuth credentials are partialy owned by the user and partialy owned by the system. For example Syndesis owns non-public credentials (OAuth Client Secret, X.509 certificate and private key) and semi-public (OAuth Client ID), that need to be used during acquisition and in a running integration together with private credentials associated with the user (username, OAuth authorization token, OAuth refresh token). This makes for two credentials with two different security and lifecycle requirements.

NOTE: Salesforce supports using x509 certificates for OAuth flow, and that should be preferred as the refreshing OAuth token can be performed within certificate's expiry i.e. not depending on the refresh token.

### Credential view model

Each credential should have a name so that the user can identify it, type: this can be username-password, x509 or OAuth tokens, internal identifier and connector label so it can be determined to what connection it can be applied to.

The credential name can be auto generated (e.g. "Salesforce credentials for user@example.com").

View model credential properties:

 * **id**        : System assigned identifier for the credential, e.g. UUID
 * **name**      : Name of the credential, for humans, e.g. "Salesforce credentials for user@example.com"
 * **connector** : Connector type (label) that the credential applies to, e.g. "salesforce"
 * **created**   : Date/time the credential was created
 * **note**      : Optional note, could be generated during credential acquisition, e.g. "Expires 23.11.2018"
 
This constitutes the view model, notice that the sensitive (secret) data is not exposed to the UI -- the UI should reference credentials by the identifier.

The view model is generated by the backend from kubernetes secrets, mapping secret name to connection, attributes above as annotations of the secret.

### Credential backend model

As credential providers would share common implementation details, surely for application of credentials, i.e. creating the kubernetes secret from acquired data, this facilitates the need for a common credential backend model. This backend model can be viewed as data transfer object between credential provider, logic that handles the application of credentials and persistence in kubernetes secrets.

The backend model is essentially a set of key-value pairs identified by the same **id** to be used in the view layer. The keys correspond to property names needed in connector configuration (i.e. set in `application.properties`) and the values are obtained during credential acquisition.

As some secrets are not exposed to the UI, e.g. secrets that are private to Syndesis such as OAuth client secret or secret/private key, the same backend model constructed during the creation of kubernetes deployment descriptor (i.e. project generation).


## Implementation

The Credential provider should implement the following Java interface:

```java
interface CredentialProvider {
    /**
     * AcquisitionMethod that this CredentialProvider supports
     */
    AcquisitionMethod acquisitionMethod();
    /**
     * Begin acquisition of the credential by returning the information
     * to the UI on how to proceed and make any arrangements needed 
     */
    Acquisition acquire(String selectedMethod, Map<String, String> methodParameters, ContinuationStore continuationStore);
    /** 
     * Process any acquisition callbacks that might occur during the
     * acquisition and produce Credentials.
     */
    Credential process(HttpServletRequest request, ContinuationStore continuationStore);
    /*
     * Gives any Credentials that are needed for deployment of the
     * integration.
     */
    Credential[] credentialsFor(Integration integration);
}
```
```java
/**
 * Information on the method of credential acquisition, needed for UI
 * to present it to the user.
 */
class AcquisitionMethod {
    String type;
    String label;
    String icon;
    String description;
}
```
```java
interface Acquisition {}
class DirectAcquisition extends Acquisition {
    Credentials credentials; 
}
class CallbackAcquisition extends Acquisition {
    String action;
    String url;
}
```
```java
class Credential {
    String id;
    Map<String, String> content;
}
```

## REST API

To interact with the credentials API:

| HTTP Verb | Path | Description |
| --------- | ---- | ----------- |
| GET | /api/{version}/connectors/{connector}/credentials | List all the ways a credential can be acquired for a {connector} |
| POST | /api/{version}/connections/{id}/credentials | Acquire new credential for connection with identifier {id} |
| \* | /api/{version}/connectors/{connector}/credentials/\*\* | Handle interaction with 3rd party services during credential acquisition |

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
  "response": {
    "method": {
      "label": "Connect to Salesforce",
      "icon": "x-salesforce",
      "description": "You will be redirected to Salesforce to authorize OAuth usage."
    }
  }
}
```
 2. Citizen user chooses "Connect to Salesforce", this results in creation of new credential acquisition:
```http
POST /api/v1/connections/__some_id__/credentials HTTP/1.1
Content-Type: application/json
Accept: application/json
```
```json
{
  "input": {
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
Location: https://{syndesis-rest}/api/v1/connectors/salesforce/credentials/callback?code=...
```
4. Credential provider for Salesforce processes the request, extracts the `code` and returns to the URL provided by the UI in 2. (`returnUrl`)
```http
GET /api/v1/connectors/salesforce/credentials/callback?code=... HTTP/1.1
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
grant_type=authorization_code&client_id={sindesis-OAuth-client-id}&client_secret={syndesis-OAuth-client-secret}&redirect_uri=https://{syndesis-rest}/api/v1/connectors/salesforce/callback&code=...
```
6. This results in credential being created for Salesforce connection

## Misc / Open Points
 
 * Session or continuations for state?
 * How to map connection to kubernetes secret, via secret name?
 * Does each tennant get it's own service account with the rights to manage secrets (read-write)
 * Do integration pods get their own service account so they (alone) can access secrets (read-only)
 * Would really like that the `Acquisition` class has method like `process` on `CredentialProvider`, not sure how that would work with clusters et al.
 

