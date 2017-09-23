# Creating Staging environment

Here are the instructions how to re-create our staging enviroment, including re-configuratiom of the CI.

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
oc adm policy add-role-to-user edit system:serviceaccount:syndesis-staging:syndesis-deployer -n syndesis-staging

# Take this token and insert it to the following circle-builds
# as environment variable OPENSHIFT_TOKEN
# - https://circleci.com/gh/syndesisio/syndesis-ui/edit#env-vars
# Also check that there is an OPENSHIFT_APISERVER env var (pointing to the staging installation)

# Grant jenkins user from syndesis-ci project the proper rights:
oc adm policy add-role-to-user admin system:serviceaccount:syndesis-ci:jenkins -n syndesis-staging

# Adapt keycloak configuration:
# - Goto to the OpenShift console
# - "Resources" -> "Config Maps"
# - Select "syndesis-keycloak-config"
# - "Actions" -> "Edit"
# - Goto to "syndesis-realm.json" field
# - Look for "redirectUris"
# - Check that the list includes http://localhost:4200/*"
#   For example
#   "redirectUris": [ "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/*", "http://localhost:4200/*" ]
# - Redeploy syndesis-keycloak

# Adapt syndesis-rest configuration:
# - Goto to config-map "syndesis-rest-config" and adapt entry "application.yml" (see above)
# - Check for "http://localhost:4200" in 'allowedOrigins:'
#   allowedOrigins: http://localhost:4200, https://syndesis-staging.b6ff.rh-idev.openshiftapps.com
# - Redeploy syndesis-rest
```
