#!/bin/bash
# Updates fuse-ignite-s2i image stream tag

# This will evaluate to the directory where the upgrade.sh script is located
export BASE_DIR=$(dirname "$(readlink -f "$0")")

APP_YAML=$(python - <<EOF
import yaml
import os

def quoted_presenter(dumper, data):
    return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='"')

yaml.add_representer(str, quoted_presenter)

# Path to the template file in the upgrade image
templateFile = os.environ['BASE_DIR'] + '/template/syndesis.yml'
if not os.path.isfile(templateFile):
    # Fallback to the template file in the syndesis repository (when running the upgrade script manually)
    templateFile = os.environ['BASE_DIR'] + '/../../install/syndesis.yml'

s2i_is = ""
with open(templateFile, 'r') as f:
    template = yaml.load(f)
    for obj in template['objects']:
        # Grab the image stream tag from the new syndesis-server-config cm's application.yml
        if 'ConfigMap' == obj['kind'] and 'syndesis-server-config' in obj['metadata']['name']:
            appYml = yaml.load(obj['data']['application.yml'])
            s2i_is = appYml['openshift']['builderImageStreamTag']
            break

with os.popen("oc get configmap syndesis-server-config -o jsonpath='{.data.application\.yml}'") as stream:
    config = yaml.load(stream)
    config["openshift"]["builderImageStreamTag"] = s2i_is
    updated = yaml.dump(config, default_flow_style=False)
    print updated.replace('\n', "\\\n").replace('\"', '\\\"')
EOF
)
APP_YAML_PATCH="{ \"data\": { \"application.yml\": \"$APP_YAML\" } }"
oc patch configmap syndesis-server-config -p "$APP_YAML_PATCH"
