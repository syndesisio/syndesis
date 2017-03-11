#!/bin/bash

echo "Installing IPaaS in ${KUBERNETES_NAMESPACE}"
oc new-project ${KUBERNETES_NAMESPACE} || oc project ${KUBERNETES_NAMESPACE}
oc update -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/serviceaccount-as-oauthclient-single-tenant.yml || oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/serviceaccount-as-oauthclient-single-tenant.yml
oc update -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas-dev-single-tenant.yml || oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas-dev-single-tenant.yml
oc new-app redhat-ipaas-dev-single-tenant \
    -p ROUTE_HOSTNAME=ipaas-testing.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:$(oc project -q):ipaas-oauth-client \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client) \
    -p OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:$(oc project -q):! role:system:build-strategy-source:$(oc project -q)" \
    -p GITHUB_OAUTH_CLIENT_ID="iocanel" \
    -p GITHUB_OAUTH_CLIENT_SECRET="changeme"
