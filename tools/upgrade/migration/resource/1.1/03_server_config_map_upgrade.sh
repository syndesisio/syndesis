#!/bin/bash
# Add the new parameter to server ConfigMap
# fetch the application.yml and append the new parameter
APP_YAML=$(python - <<EOF
import yaml
import os

def quoted_presenter(dumper, data):
    return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='"')

yaml.add_representer(str, quoted_presenter)

with os.popen("oc get configmap syndesis-server-config -o jsonpath='{.data.application\.yml}'") as stream:
  config = yaml.load(stream)
  config["controllers"] = config.get("controllers", dict())
  config["controllers"]["integrationStateCheckInterval"] = 60
  config["monitoring"] = config.get("monitoring", dict())
  config["monitoring"]["kind"] = "default"
  config["features"] = config.get("features", dict())
  config["features"]["monitoring"] = config["features"].get("monitoring", dict())
  config["features"]["monitoring"]["enabled"] = True
  config["builderImageStreamTag"] = "fuse-ignite-s2i:1.4"
  updated = yaml.dump(config, default_flow_style=False)
  print updated.replace('\n', "\\\n").replace('\"', '\\\"')
EOF
)
APP_YAML_PATCH="{ \"data\": { \"application.yml\": \"$APP_YAML\" } }"
oc patch configmap syndesis-server-config -p "$APP_YAML_PATCH"
