#!/bin/bash
# Add new datavirt parameters to ui ConfigMap
# fetch the config.json and append the new parameters for datavirt
CONFIG_JSON=$(python - <<EOF
import json
import os

with os.popen("oc get configmap syndesis-ui-config -o jsonpath='{.data.config\.json}'") as stream:
  out = stream.read().strip()
  config = json.loads(out)
  config["datavirt"] = config.get("datavirt", dict())
  config["datavirt"]["dvUrl"] = "/vdb-builder/v1/"
  config["datavirt"]["enabled"] = 0
  updated = json.dumps(config, indent=4)
  print updated.replace('\n', "\\\n").replace('\"', '\\\"')
EOF
)
CONFIG_JSON_PATCH="{ \"data\": { \"config.json\": \"$CONFIG_JSON\" } }"
oc patch configmap syndesis-ui-config -p "$CONFIG_JSON_PATCH"
