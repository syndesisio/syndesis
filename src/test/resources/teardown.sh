#!/bin/bash

echo "Remove Syndesis from ${KUBERNETES_NAMESPACE}"
oc project ${KUBERNETES_NAMESPACE}

echo "Displaying the list of Pods in project: ${KUBERNETES_NAMESPACE}"
oc get pods -n ${KUBERNETES_NAMESPACE}

echo "Removing Syndesis from ${KUBERNETES_NAMESPACE}"
oc process syndesis \
    ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com \
    KEYCLOAK_ROUTE_HOSTNAME=${KUBERNETES_NAMESPACE}-keycloack.b6ff.rh-idev.openshiftapps.com \
    OPENSHIFT_MASTER=$(oc whoami --show-server) \
    GITHUB_OAUTH_CLIENT_ID=${GITHUB_OAUTH_CLIENT_ID} \
    GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_OAUTH_CLIENT_SECRET} \
    OPENSHIFT_OAUTH_CLIENT_ID=$(oc project -q) \ -n ${KUBERNETES_NAMESPACE}  | oc delete -f - -n ${KUBERNETES_NAMESPACE}
