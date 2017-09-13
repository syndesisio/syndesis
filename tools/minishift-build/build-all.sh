#!/usr/bin/env bash

set -e

eval $(minishift docker-env)
oc config use-context minishift
oc login -u developer -p developer
d="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. $d/integration-runtime.sh
. $d/rest.sh
. $d/atlasmap.sh
. $d/verifier.sh
. $d/ui.sh

watch oc get pods
