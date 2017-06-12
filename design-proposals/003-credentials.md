## Credential support

* Issue: https://github.com/syndesisio/syndesis-rest/issues/386
* Sprint: 
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

### User Story

As a citizen user I would like to connect to 3rd party service that require authenticate using credentials. Credentials would need to be stored in Syndesis so I can manage and assign them to other integrations using same 3rd party services. For example when connecting to Salesforce the I need to provide username/password authentication only once to authorize Syndesis's access using OAuth, and in order to do that I would like to choose "connect to Salesforce" in Syndesis and not be prompted again for authorization.

My requirements are:

 * Simplicity in authorization flow ("connect to Salesforce" button)
 * Secure storage of authorization data (credentials)
 * Ability to manage credentials: delete and update
 * Ability to associate different credentials to different integrations (many to many)


