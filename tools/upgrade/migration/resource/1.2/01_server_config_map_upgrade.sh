#!/bin/bash
# Updates fuse-ignite-s2i image stream tag
APP_YAML=$(python - <<EOF
import yaml
import os

def quoted_presenter(dumper, data):
    return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='"')

yaml.add_representer(str, quoted_presenter)

with os.popen("oc get configmap syndesis-server-config -o jsonpath='{.data.application\.yml}'") as stream:
  config = yaml.load(stream)
  config["openshift"]["builderImageStreamTag"] = "fuse-ignite-s2i:1.5"
  updated = yaml.dump(config, default_flow_style=False)
  print updated.replace('\n', "\\\n").replace('\"', '\\\"')
EOF
)
APP_YAML_PATCH="{ \"data\": { \"application.yml\": \"$APP_YAML\" } }"
oc patch configmap syndesis-server-config -p "$APP_YAML_PATCH"
