#!/bin/bash

set -euo pipefail

SYNUI_REV=$(oc get deploymentconfig syndesis-ui --template={{.status.latestVersion}})
echo "syndesis-ui current at revision ${SYNUI_REV}"

POD_PREFIX="syndesis-ui-${SYNUI_REV}"
SYNPOD=$(oc get pods | grep ${POD_PREFIX} | awk {'print $1'})
echo "Currently running syndesis-ui pod: ${SYNPOD}"

SYNIP=$(oc get pod ${SYNPOD} --template={{.status.podIP}})

#
# Restore the existing service
#
oc replace --force -f - <<EOF
{
  "apiVersion": "v1",
  "kind": "Service",
  "metadata": {
    "name": "syndesis-ui",
    "labels": {
      "app": "syndesis",
      "syndesis.io/app": "syndesis",
      "syndesis.io/type": "infrastructure",
      "syndesis.io/component": "syndesis-ui"
    }
  },
  "spec": {
     "ports": [
       {
         "port": 80,
         "protocol": "TCP",
         "targetPort": 8080
       }
     ],
     "selector": {
       "app": "syndesis",
       "syndesis.io/app": "syndesis",
       "syndesis.io/component": "syndesis-ui"
     }
  }
}
EOF

#
# Restore the original endpoint
# (Cannot do in same replace as service - generates error!)
#
oc replace --force -f - <<EOF
{
  "apiVersion": "v1",
  "kind": "Endpoints",
  "metadata": {
    "labels": {
      "app": "syndesis",
      "syndesis.io/app": "syndesis",
      "syndesis.io/component": "syndesis-ui",
      "syndesis.io/type": "infrastructure"
    },
    "name": "syndesis-ui"
  },
  "subsets": [
    {
      "addresses": [
        {
          "ip": "${SYNIP}",
		  "targetRef": {
            "kind": "Pod",
            "name": "${SYNPOD}",
            "namespace": "syndesis"
          }
        }
      ],
      "ports": [
        {
          "port": 8080,
          "protocol": "TCP"
        }
      ]
    }
  ]
}
EOF

# also scale back up the operator
oc scale --replicas=1 dc syndesis-operator
