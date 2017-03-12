#!/bin/bash

echo "Removing IPaaS from ${KUBERNETES_NAMESPACE}"
oc process redhat-ipaas-dev-single-tenant  \
    ROUTE_HOSTNAME=ipaas-testing.b6ff.rh-idev.openshiftapps.com \
    OPENSHIFT_MASTER=$(oc whoami --show-server) \
    OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:$(oc project -q):ipaas-oauth-client \
    OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client) \
    OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:$(oc project -q):! role:system:build-strategy-source:$(oc project -q)" \
    GITHUB_OAUTH_CLIENT_ID="iocanel" \
    GITHUB_OAUTH_CLIENT_SECRET="changeme" -n ${KUBERNETES_NAMESPACE}  | oc delete -f - -n ${KUBERNETES_NAMESPACE}
