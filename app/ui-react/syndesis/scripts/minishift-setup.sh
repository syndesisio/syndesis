#!/bin/bash

export PS4='+(${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'
set -x

set -euo pipefail
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

# Lets try to detect the local machine ip..
LOCAL_IP="${SYNDESIS_DEV_LOCAL_IP:=$(./node_modules/.bin/my-local-ip)}"
echo "Local IP is: ${LOCAL_IP}"

# Let's also just back this up in case.
mv -f public/config.json public/config.json.bak || true
cp config.minishift.json public/config.json

OS_HOST=$(oc status | head -n 1 | sed s/.*https/https/)

sed -i.bu "s#syndesis.192.168.64.2.nip.io#$(oc get route syndesis  --template={{.spec.host}})#" public/config.json
sed -i.bu "s#https://192\.168\.64\.2:8443#${OS_HOST}#" public/config.json
sed -i.bu "s/Syndesis/Syndesis - DEVELOPMENT/" public/config.json
rm public/config.json.bu

# scale down the operator to stop it replacing our local changes
oc scale --replicas=0 dc syndesis-operator
oc replace --force -f - <<EOF
{
  "kind": "List",
  "apiVersion": "v1",
  "items": [
    {
      "apiVersion": "v1",
      "kind": "Service",
      "metadata": {
        "labels": {
          "app": "syndesis",
          "component": "syndesis-ui"
        },
        "name": "syndesis-ui"
      },
      "spec": {
        "ports": [
          {
            "port": 80,
            "protocol": "TCP",
            "targetPort": 3000,
            "name": "http"
          }
        ],
        "selector": {}
      }
    },
    {
      "apiVersion": "v1",
      "kind": "Endpoints",
      "metadata": {
        "name": "syndesis-ui"
      },
      "subsets": [
        {
          "addresses": [
            {
              "ip": "${LOCAL_IP}"
            }
          ],
          "ports": [
            {
              "name": "http",
              "port": 3000
            }
          ]
        }
      ]
    }
  ]
}
EOF
