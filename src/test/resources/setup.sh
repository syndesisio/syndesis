#!/bin/bash

#   External Environment Variables
#
#1. KUBERNETES_NAMESPACE:                   The target namespace.
#2. SYNDESIS_TEMPLATE_TYPE:                 The template type to use (e.g. syndesis, syndesis-restricted, syndesis-ephemeral-restricted.
#3. SYNDESIS_TEMPLATE_URL:                  The full url to the templates to use.
#4. OPENSHIFT_TEMPLATE_FROM_WORKSPACE:      Flag to enable reading the template from the ${WORKSPACE} environment variable.
#5. OPENSHIFT_TEMPLATES_FROM_GITHUB_COMMIT: The sha of the actual commit to use. Meant to be used for pull request validation (currently unused?).
#
#   Template Parameter Values:
#
#   OPENSHIFT_MASTER
#   GITHUB_OAUTH_CLIENT_ID
#   GITHUB_OAUTH_CLIENT_SECRET

#Configure the SYNDESIS_TEMPLATE_TYPE
if [ -z "${SYNDESIS_TEMPLATE_TYPE}" ]; then
    SYNDESIS_TEMPLATE_TYPE="syndesis"
fi

#Configure the SYNDESIS_TEMPLATE_URL
if [ -n "${SYNDESIS_TEMPLATE_URL}" ]; then
    true
elif [ -n "${OPENSHIFT_TEMPLATE_FROM_WORKSPACE}" ]; then
    SYNDESIS_TEMPLATE_URL="file:///${WORKSPACE}/syndesis.yml"
elif [ -n "${OPENSHIFT_TEMPLATES_FROM_GITHUB_COMMIT}" ]; then
    SYNDESIS_TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/${OPENSHIFT_TEMPLATES_GIT_COMMIT}/${SYNDESIS_TEMPLATE_TYPE}.yml"
else
    SYNDESIS_TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/${SYNDESIS_TEMPLATE_TYPE}.yml"
fi

#Configure OPENSHIFT_MASTER
if [ -z "${OPENSHIFT_MASTER}" ]; then
    OPENSHIFT_MASTER="$(oc whoami --show-server)"
fi

# We pass the namespace on each command individually, because when this script is run inside a pod, all commands default to the pod namespace (ignoring commands like `oc project` etc)
echo "Installing Syndesis in ${KUBERNETES_NAMESPACE} from: ${SYNDESIS_TEMPLATE_URL}"
oc project ${KUBERNETES_NAMESPACE}

oc create -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}  || oc replace -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}

oc new-app syndesis \
    -p ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    -p KEYCLOAK_ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}-keycloack.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=${OPENSHIFT_MASTER} \
    -p GITHUB_OAUTH_CLIENT_ID=${GITHUB_OAUTH_CLIENT_ID} \
    -p GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_OAUTH_CLIENT_SECRET} \
    -p OPENSHIFT_OAUTH_CLIENT_ID=$(oc project -q) \
    -n ${KUBERNETES_NAMESPACE}


#Move image streams (one by one) inside the test namespace
mkdir -p /tmp/syndesis-test-resources/
for i in `oc get is -n syndesis-ci | grep -v NAME | cut -d" " -f1`; do
    oc export is $i -n syndesis-ci > /tmp/syndesis-test-resources/$i.yml
    oc create -n "${KUBERNETES_NAMESPACE}" -f /tmp/syndesis-test-resources/$i.yml 2> /dev/null || oc replace -n "${KUBERNETES_NAMESPACE}" -f /tmp/syndesis-test-resources/$i.yml;
done
