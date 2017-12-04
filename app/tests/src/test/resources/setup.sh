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

#Configure the SYNDESIS_TEMPLATE_TYPE
if [ -z "${SYNDESIS_TEMPLATE_TYPE}" ]; then
    SYNDESIS_TEMPLATE_TYPE="syndesis-ci"
fi

if [ -z "${KUBERNETES_NAMESPACE}" ]; then
    KUBERNETES_NAMESPACE="$(oc project -q)"
fi

#Configure the SYNDESIS_TEMPLATE_URL
if [ -n "${SYNDESIS_TEMPLATE_URL}" ]; then
    true
elif [ -n "${OPENSHIFT_TEMPLATE_FROM_WORKSPACE}" ]; then
    SYNDESIS_TEMPLATE_URL="${WORKSPACE}/${SYNDESIS_TEMPLATE_TYPE}.yml"
elif [ -n "${OPENSHIFT_TEMPLATES_FROM_GITHUB_COMMIT}" ]; then
    SYNDESIS_TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/${OPENSHIFT_TEMPLATES_GIT_COMMIT}/${SYNDESIS_TEMPLATE_TYPE}.yml"
else
    SYNDESIS_TEMPLATE_URL="https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/${SYNDESIS_TEMPLATE_TYPE}.yml"
fi

#Configure OPENSHIFT_MASTER
if [ -z "${OPENSHIFT_MASTER}" ]; then
    OPENSHIFT_MASTER="$(oc whoami --show-server)"
fi

if [ -z "${DEMO_DATA_ENABLED}" ]; then
    DEMO_DATA_ENABLED=false
fi

# We pass the namespace on each command individually, because when this script is run inside a pod, all commands default to the pod namespace (ignoring commands like `oc project` etc)
echo "Installing Syndesis in ${KUBERNETES_NAMESPACE} from: ${SYNDESIS_TEMPLATE_URL}"
oc project ${KUBERNETES_NAMESPACE}


# If env variable `SYNDESIS_RELEASED_IMAGES` IS provided by template will be used
if [ -n "${SYNDESIS_RELEASED_IMAGES}" ]; then
    echo "ImageStreams specified by template will be used"
    # no op required
else
    # Move image streams (one by one) inside the test namespace
    echo "Local ImageStreams will be used"
fi

oc create -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}  || oc replace -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}

oc new-app ${SYNDESIS_TEMPLATE_TYPE}  \
    -p ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    -p OPENSHIFT_MASTER=${OPENSHIFT_MASTER} \
    -p OPENSHIFT_OAUTH_CLIENT_ID=$(oc project -q) \
    -p DEMO_DATA_ENABLED=${DEMO_DATA_ENABLED} \
    -p TEST_SUPPORT_ENABLED=true \
    -n ${KUBERNETES_NAMESPACE}
