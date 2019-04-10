# Global user settings

* Issue: https://github.com/syndesisio/syndesis-project/issues/13
* Sprint: 14
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

## Background
Create a page or set of pages that lets me configure:

* Client ID/Secret management
* Github setup
* OpenShift Namespace settings.

As a user I need to be able to see and/or configure settings that are global for the Syndesis installation.  I would expect to see some of the following things in the global settings:

#### Database
Syndesis developers sometimes need to export and import the underlying database.  Currently this is handled via menu items in the user dropdown menu.  However it may make more sense for these items to live in the global settings page instead, where the options would only be visible when syndesis has been started with the appropriate flags.

#### OAuth
This section of the global configuration will contain the OAuth client parameters for external applications that my integrations connect to such as Salesforce, Twitter and other applications that Syndesis supports.  I should be able to review and edit the parameters associated with each application.  These settings may also be part of the flow of creating a connection, I'd expect to see the same fields to enter in here.

#### Future settings

It's possible at some point in the future the settings pages could encompass other settings and are listed here for completeness, but are out of the scope of this proposal.  Some examples:

##### GitHub
When Syndesis creates an integration for me the project needs to be stored somewhere.  I would expect to be able to configure what Github organization my integrations will be created in at least initially, or at least see where my projects will be created.

##### OpenShift
As a user I know that my integrations are run on the system under some namespace. I may want to be able to see this information, and if possible I may want to be able to change it or initially set it to suit what I want to do.

## User Story

*TBD*

## API
The API entry point for settings will be at /setup/

Specifically for OAuth client settings the API will be at /setup/oauth-apps/* and will support standard REST verbs.  A single entity will be in this shape:

```json
{
  "id": "twitter",
  "name": "Twitter",
  "icon": "fa-twitter",
  "clientId": "foo",
  "clientSecret": ""
}
```

When posting an update to the server, only the id, clientId and clientSecret fields will be required.  Other fields could be present based on the requirements of the target applications OAuth implementation.

The ConfigurationProperty model needs to be enhanced to signal to the settings handler which connection properties are application client IDs and secrets.  A tags field could be used to hold these markers to help correlate these events.

## Persistence
OAuth client related parameters and parameters that affect connections and integrations will likely be stored in a database.

Some configuration parameters could outside of the database and instead be parameters used when running Syndesis.  Its possible some of these probably can't be easily changed at runtime.

## UI
The settings page should be accessed via the main navigation.  Related settings should be logically organized together.

## Misc / Open Points

## Remarks
