#!/bin/bash
#   External Environment Variables
#
#1. KUBERNETES_NAMESPACE:                   The target namespace.
#2. SYNDESIS_TEMPLATE_TYPE:                 The template type to use (e.g. syndesis or syndesis-dev.

#Configure the SYNDESIS_TEMPLATE_TYPE
if [ -z "${SYNDESIS_TEMPLATE_TYPE}" ]; then
    SYNDESIS_TEMPLATE_TYPE="syndesis"
fi

echo "Remove Syndesis from ${KUBERNETES_NAMESPACE}"
oc project ${KUBERNETES_NAMESPACE}

echo "Displaying the list of Pods in project: ${KUBERNETES_NAMESPACE}"
oc get pods -n ${KUBERNETES_NAMESPACE}

echo "Removing Syndesis from ${KUBERNETES_NAMESPACE}"
oc process ${SYNDESIS_TEMPLATE_TYPE} -n ${KUBERNETES_NAMESPACE} \
    ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    OPENSHIFT_MASTER=$(oc whoami --show-server) \
    OPENSHIFT_PROJECT=$(oc project -q) \
    OPENSHIFT_OAUTH_CLIENT_SECRET="none" | oc delete -f - -n ${KUBERNETES_NAMESPACE}

oc delete templates ${SYNDESIS_TEMPLATE_TYPE}
