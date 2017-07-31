# Creating Staging environment

These are the instructions how to re-create our staging enviroment.

```
# Create new project
oc new-project syndesis-staging

# Create new SA as OAuthClient
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/support/serviceaccount-as-oauthclient-restricted.yml

# Get the GitHub params from attached github organization 'syndesisio', app 'RedHat iPaaS - STAGING ONLY'
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...

# Create template
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-restricted.yml

# Instantiate application
oc new-app syndesis-restricted \
    -p ROUTE_HOSTNAME=syndesis-staging.b6ff.rh-idev.openshiftapps.com \
    -p KEYCLOAK_ROUTE_HOSTNAME=syndesis-staging-keycloack.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client) \
    -p GITHUB_OAUTH_CLIENT_ID=${GITHUB_CLIENT_ID} \
    -p GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_CLIENT_SECRET} \
    -p INSECURE_SKIP_VERIFY=true
    
# Wait until everything's up ....

# Create deployer SA and get the deploy token
oc create sa syndesis-deployer
oc sa get-token syndesis-deployer

# Take this token and insert it to the followin circle-builds
# as environment variable OPENSHIFT_TOKEN:
# - https://circleci.com/gh/syndesisio/syndesis-ui/edit#env-vars
```
