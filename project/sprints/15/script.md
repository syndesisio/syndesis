# Demo Walkthrough Sprint 15

## Preparations

* Reset Database
* Enter client id and secrets for Twitter and Salesforce in Global Section

## Walkthrough

### OAuth Flow

* Go to "Connections" --> "Create"
* Select Twitter
* Select "Connect"
* Fill out auth form if not already connected
* Save connections
* "Edit" connection again, then "Validate"
* Same for Salesforce

### Basic Filter

* New Integration
* Select Twitter (mention) and Salesforce (upsert_contact) connection
* Add "Step" --> "Basic Filter"
* Show autocompletion of path
* Select "Status.Text", "contains (ignore case)", "Demo Time"

### Data Mapper Fixes

* Add "Mapping" Step after filter
* Select "Status.user.screename" connect with "TwitterScreenName__c"
* Scroll endpoints off-screen, show that lines stay

### GitHub

* Publish integration
* Show overview page with status ("Progress")
* Goto GitHub webpage and check generated Repo
* Show that only a single commit happened
* Show generated files (with filter and mapper steps)

### OpenShift Console

* Go to the OpenShift console
* Show deployments
* Show logs

### Tweet

* Goto twitter (from personal account)
* Send a tweet referencing "@syndesis_d_test" (or the account with is connected), text "Demo Time"
* Check logs that it reaches salesforce connector
* Send a tweet referencing "@syndesis_d_test" (or the account with is connected), text "Awesome"
* Check logs that it does not reach salesforce connector
