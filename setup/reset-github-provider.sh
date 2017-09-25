#!/usr/bin/env bash

usage() {
    echo "Usage: $(basename $0) <KEYCLOAK_URL>"
    exit 1
}

KEYCLOAK_URL=${1}
if [ -z "${KEYCLOAK_URL}" ]; then
    usage
fi

if ! hash jq 2>/dev/null; then
    echo "This script requires \`jq\` - please see https://stedolan.github.io/jq/download/
or run \`brew install jq\` if you're on OS X"
    exit 1
fi

KC_TOKEN=$(curl "${KEYCLOAK_URL}/auth/realms/master/protocol/openid-connect/token" \
  -d "client_id=admin-cli" \
  -d "username=$(oc get secrets -ojsonpath={.data.username} syndesis-keycloak-admin|base64 -d)" \
  -d "password=$(oc get secrets -ojsonpath={.data.password} syndesis-keycloak-admin|base64 -d)" \
  -d "grant_type=password" \
  -fsSLk | \
jq -r .access_token)

KC_GITHUB_IDP=$(curl "${KEYCLOAK_URL}/auth/admin/realms/syndesis/identity-provider/instances/github" \
  -fsSLk \
  -k -vvv \
  -H "Authorization: Bearer ${KC_TOKEN}" | \
  sed -e 's/"clientId":"[a-zA-Z0-9]\+",/"clientId":"dummy",/' -e 's/"clientSecret":"\*\+",/"clientSecret":"dummy",/')

curl "${KEYCLOAK_URL}/auth/admin/realms/syndesis/identity-provider/instances/github" \
  -XPUT \
  -fsSLk \
  -k -vvv \
  -H "Authorization: Bearer ${KC_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "${KC_GITHUB_IDP}"
