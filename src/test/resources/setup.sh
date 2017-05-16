#!/bin/bash

#Configure the TEMPLATE_URL
if [ ! -z ${OPENSHIFT_TEMPLATE_FROM_WORKSPACE} ]; then
    TEMPLATE_URL="file:///${WORKSPACE}/syndesis.yml"
elif [ ! -z ${OPENSHIFT_TEMPLATES_FROM_GITHUB_COMMIT} ]; then
    TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/openshift-templates/${OPENSHIFT_TEMPLATES_GIT_COMMIT}/syndesis.yml"
else
    TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/openshift-templates/master/syndesis.yml"
fi

#Configure OPENSHIFT_MASTER
if [ -z ${OPENSHIFT_MASTER} ]; then
    OPENSHIFT_MASTER="$(oc whoami --show-server)"
fi


# We pass the namespace on each command individually, because when this script is run inside a pod, all commands default to the pod namespace (ignoring commands like `oc project` etc)
echo "Installing Syndesis in ${KUBERNETES_NAMESPACE} from: ${TEMPLATE_URL}"
oc project ${KUBERNETES_NAMESPACE}

oc create -f ${TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}  || oc replace -f ${TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}

oc new-app syndesis \
    -p ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    -p KEYCLOAK_ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}-keycloack.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p GITHUB_OAUTH_CLIENT_ID=${GITHUB_OAUTH_CLIENT_ID} \
    -p GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_OAUTH_CLIENT_SECRET} \
    -p OPENSHIFT_OAUTH_CLIENT_ID=${OPENSHIFT_MASTER} \
    -n ${KUBERNETES_NAMESPACE}


#Move image streams (one by one) inside the test namespace
mkdir -p /tmp/syndesis-test-resources/
for i in `oc get is -n syndesis-ci | grep -v NAME | cut -d" " -f1`; do
    oc export is $i -n syndesis-ci > /tmp/syndesis-test-resources/$i.yml
    oc create -n "${KUBERNETES_NAMESPACE}" -f /tmp/syndesis-test-resources/$i.yml 2> /dev/null || oc replace -n "${KUBERNETES_NAMESPACE}" -f /tmp/syndesis-test-resources/$i.yml;
done
