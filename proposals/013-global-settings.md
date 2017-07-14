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

#### Github
When Syndesis creates an integration for me the project needs to be stored somewhere.  I would expect to be able to configure what Github organization my integrations will be created in at least initially, or at least see where my projects will be created.

#### Openshift
As a user I know that my integrations are run on the system under some namespace.  I may want to be able to see this information, and if possible I may want to be able to change it or initially set it to suit what I want to do.

#### OAuth
This section of the global configuration will contain the OAuth client parameters for external applications that my integrations connect to such as Salesforce, Twitter and other applications that Syndesis supports.  I should be able to review and edit the parameters associated with each application.  These settings may also be part of the flow of creating a connection, I'd expect to see the same fields to enter in here.

Some global configuration options may affect connections and integrations that are already deployed and running.  When the configuration is changed, I would expect that those changes should somehow be propogated to any integrations that I'm running.

#### Database
Syndesis developers sometimes need to export and import the underlying database.  Currently this is handled via menu items in the user dropdown menu.  However it may make more sense for these items to live in the global settings page instead, where the options would only be visible when syndesis has been started with the appropriate flags.

## User Story

*TBD*

## Persistence

Some configuration parameters will live outside of any database and instead be parameters used when running Syndesis.  Its possible some of these probably can't be easily changed at runtime.  OAuth client related parameters and paramters that affect connections and integrations will likely be stored in a database; it should be possible for a user to change these as needed and have the changes propogated.

## API

*Question: Do the general settings need a specific API? Or does one exist already?*

The OAuth client section of the global configuration will use the API defined [in this document](./003-credentials.md##rest-api).

## UI

The settings page should be accessed via the main navigation.  Related settings should be logically organized together.  The OAuth client related settings for each endpoint will most likely share [previously developed components for credentials](./003-credentials.md).

## Misc / Open Points

## Remarks
