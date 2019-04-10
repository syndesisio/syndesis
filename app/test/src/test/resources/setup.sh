#!/bin/bash

#   External Environment Variables
#
#1. KUBERNETES_NAMESPACE:                   The target namespace.
#2. OPENSHIFT_MASTER                        The openshift master url.
#3. DEMO_DATA_ENABLED                       Demo data flag.
#4. MINISHIFT_ENABLED                       Minishift flag.
#
#   Template Parameter Values:
#   OPENSHIFT_MASTER

if [ -z "${KUBERNETES_NAMESPACE}" ]; then
    KUBERNETES_NAMESPACE="$(oc project -q)"
fi
if [ -z "${OPENSHIFT_MASTER}" ]; then
    OPENSHIFT_MASTER="$(oc whoami --show-server)"
fi
if [ -z "${DEMO_DATA_ENABLED}" ]; then
    DEMO_DATA_ENABLED=false
fi

SYNDESIS_TEMPLATE_TYPE="syndesis"
SYNDESIS_TEMPLATE_URL="${WORKSPACE:-$(pwd)/../..}/install/${SYNDESIS_TEMPLATE_TYPE}.yml"
SYNDESIS_OAUTHCLIENT_URL="${WORKSPACE:-$(pwd)/../..}/install/support/serviceaccount-as-oauthclient-restricted.yml"

ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com

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

oc create -f ${SYNDESIS_OAUTHCLIENT_URL}  || oc replace -f ${SYNDESIS_OAUTHCLIENT_URL}

if [ "true" == "$MINISHIFT_ENABLED" ]; then
    echo "Applying minishift specific configuration"
    oc login -u system:admin
    oc adm policy add-cluster-role-to-user system:auth-delegator -z syndesis-oauth-client
    oc login -u admin -p admin
    oc project ${KUBERNETES_NAMESPACE}
    ROUTE_HOSTNAME=syndesis.$(minishift ip).nip.io
fi

oc create -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}  || oc replace -f ${SYNDESIS_TEMPLATE_URL} -n ${KUBERNETES_NAMESPACE}
oc new-app --template=${SYNDESIS_TEMPLATE_TYPE} \
    -p ROUTE_HOSTNAME=$ROUTE_HOSTNAME \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p DEMO_DATA_ENABLED=${DEMO_DATA_ENABLED} \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)
