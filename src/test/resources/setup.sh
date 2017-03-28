#!/bin/bash

# We pass the namespace on each command individually, because when this script is run inside a pod, all commands default to the pod namespace (ignoring commands like `oc project` etc)
echo "Installing IPaaS in ${KUBERNETES_NAMESPACE}"

oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/serviceaccount-as-oauthclient-single-tenant.yml -n ${KUBERNETES_NAMESPACE} || oc replace -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/serviceaccount-as-oauthclient-single-tenant.yml -n ${KUBERNETES_NAMESPACE}
oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas-single-tenant.yml -n ${KUBERNETES_NAMESPACE}  || oc replace -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas-single-tenant.yml -n ${KUBERNETES_NAMESPACE}
oc new-app redhat-ipaas-single-tenant \
    -p ROUTE_HOSTNAME=ipaas-testing.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:${KUBERNETES_NAMESPACE}:ipaas-oauth-client \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client -n ${KUBERNETES_NAMESPACE}) \
    -p OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:${KUBERNETES_NAMESPACE}:! role:system:build-strategy-source:${KUBERNETES_NAMESPACE}" \
    -p GITHUB_OAUTH_CLIENT_ID="iocanel" \
    -p GITHUB_OAUTH_CLIENT_SECRET="changeme" \
    -n ${KUBERNETES_NAMESPACE}


#Move image streams (one by one) inside the test namespace
mkdir -p target/test-resources
for i in `oc get is -n ipaas-ci | grep -v NAME | cut -d" " -f1`; do
    oc export is $i -n ipaas-ci > target/test-resources/$i.yml
    oc create -n "${KUBERNETES_NAMESPACE}" -f target/test-resources/$i.yml 2> /dev/null || oc replace -n "${KUBERNETES_NAMESPACE}" -f target/test-resources/$i.yml;
done
