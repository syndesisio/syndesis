# Creating Staging environment

Here are the instructions how to re-create our staging enviroment, including re-configuration of the CI.

```
# Create new project
oc new-project syndesis-staging

# Create new SA as OAuthClient
oc create -f support/serviceaccount-as-oauthclient-restricted.yml

# Create template
oc create -f syndesis.yml

# Instantiate application
oc new-app --template=syndesis \
    -p ROUTE_HOSTNAME=$(oc project -q).b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)

# Wait until everything's up ....

# Create deployer SA and get the deploy token
oc create sa syndesis-deployer
oc sa get-token syndesis-deployer
oc adm policy add-role-to-user edit system:serviceaccount:$(oc project -q):syndesis-deployer -n $(oc project -q)

# Take this token and insert it to the following circle-builds
# as environment variable OPENSHIFT_TOKEN
# - https://circleci.com/gh/syndesisio/syndesis-ui/edit#env-vars
# Also check that there is an OPENSHIFT_APISERVER env var (pointing to the staging installation)

# Grant jenkins user from syndesis-ci project the proper rights:
oc adm policy add-role-to-user admin system:serviceaccount:syndesis-ci:jenkins -n $(oc project -q)

# Adapt syndesis-server configuration:
# - Goto to config-map "syndesis-server-config" and adapt entry "application.yml" (see above)
# - Check for "http://localhost:4200" in 'allowedOrigins:'
#   allowedOrigins: http://localhost:4200, https://syndesis-staging.b6ff.rh-idev.openshiftapps.com
# - Redeploy syndesis-server
```
