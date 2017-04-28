#!/bin/bash

# We pass the namespace on each command individually, because when this script is run inside a pod, all commands default to the pod namespace (ignoring commands like `oc project` etc)
echo "Installing IPaaS in ${KUBERNETES_NAMESPACE}"

oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas.yml -n ${KUBERNETES_NAMESPACE}  || oc replace -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas.yml -n ${KUBERNETES_NAMESPACE}

oc new-app ipaas \
    -p ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    -p KEYCLOAK_ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}-keycloack.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p GITHUB_OAUTH_CLIENT_ID=${GITHUB_OAUTH_CLIENT_ID} \
    -p GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_OAUTH_CLIENT_SECRET} \
    -p OPENSHIFT_OAUTH_CLIENT_ID=$(oc project -q) \
    -n ${KUBERNETES_NAMESPACE}


#Move image streams (one by one) inside the test namespace
mkdir -p target/test-resources
for i in `oc get is -n ipaas-ci | grep -v NAME | cut -d" " -f1`; do
    oc export is $i -n ipaas-ci > target/test-resources/$i.yml
    oc create -n "${KUBERNETES_NAMESPACE}" -f target/test-resources/$i.yml 2> /dev/null || oc replace -n "${KUBERNETES_NAMESPACE}" -f target/test-resources/$i.yml;
done
