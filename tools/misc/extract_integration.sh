#!/bin/sh

# Extract an integration's source and config so that it can be run locally
#
# Usage:
# extract_integration.sh <integration name> [<dir>]
#
# <integration name> : Name of the integration pod or integration name
# <dir> : Where to store the source (default: .)
#
# Required tools:
# - bash
# - oc
# - jq
# - base64

set -euo pipefail

pod=${1:-xxx}
if [ $pod = "xxx" ]; then
  echo "Usage $0 <integration pod> [directory]"
  exit 1
fi
dest=${2:-.}
if [ ! -d $dest ]; then
  echo "$dest is not a directory"
  exit 1
fi

oc cp ${pod}:/tmp/src $dest

secret=$(oc get pod $pod -o json | jq -r ".spec.volumes[]|select(.name==\"secret-volume\")|.secret.secretName")
mkdir $dest/config || true
oc get secret $secret -o jsonpath="{.data.application\.properties}" | base64 --decode > $dest/config/application.properties
