#!/bin/bash

set -euo pipefail

# Backup the existing service
oc replace --force -f - <<EOF
  {
    "apiVersion": "v1",
    "kind": "Service",
    "metadata": {
      "name": "syndesis-ui",
      "labels": {
        "app": "syndesis",
        "component": "syndesis-ui"
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
        "component": "syndesis-ui"
      }
    }
  }

EOF
