#!/bin/bash
# Add the new parameter to server ConfigMap
# fetch the application.yml and append the new parameter
APP_YAML="$(oc get configmap syndesis-server-config -o jsonpath='{.data.application\.yml}')
  integrationStateCheckInterval: 60
monitoring:
  kind: default
features:
  monitoring:
    enabled: true
"
# change newlines to \n
APP_YAML=${APP_YAML//$'\n'/\\n}
# change quotes to \"
APP_YAML=${APP_YAML//\"/\\\"}
# generate the patch
APP_YAML_PATCH="{ \"data\": { \"application.yml\": \"$APP_YAML\" } }"
oc patch configmap syndesis-server-config -p "$APP_YAML_PATCH"
