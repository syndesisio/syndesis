
#!/bin/bash
#
#

set -euo pipefail
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

# Lets try to detect the local machine ip..
LOCAL_IP="$(./node_modules/.bin/my-local-ip)"
echo "DETECTED Local IP is: ${LOCAL_IP}"

LOCAL_IP="${1:-$LOCAL_IP}"

sed "s/192.168.64.2/$(minishift ip)/" src/config.json.minishift > src/config.json
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
            "targetPort": 4200,
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
              "port": 4200
            }
          ]
        }
      ]
    }
  ]
}
EOF
