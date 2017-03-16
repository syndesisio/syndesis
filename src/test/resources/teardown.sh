#!/bin/bash

echo "Removing IPaaS from ${KUBERNETES_NAMESPACE}"
oc process redhat-ipaas-dev-single-tenant  \
    ROUTE_HOSTNAME=ipaas-testing.b6ff.rh-idev.openshiftapps.com \
    OPENSHIFT_MASTER=$(oc whoami --show-server) \
    OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:${KUBERNETES_NAMESPACE}:ipaas-oauth-client \
    OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client -n ${KUBERNETES_NAMESPACE}) \
    OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:${KUBERNETES_NAMESPACE}:! role:system:build-strategy-source:${KUBERNETES_NAMESPACE}" \
    GITHUB_OAUTH_CLIENT_ID="iocanel" \
    GITHUB_OAUTH_CLIENT_SECRET="changeme" -n ${KUBERNETES_NAMESPACE}  | oc delete -f - -n ${KUBERNETES_NAMESPACE}
