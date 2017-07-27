#!/bin/sh

set -e

eval $(minishift docker-env)
oc config use-context minishift
oc login -u developer -p developer
d=$(dirname `realpath $0`)
. $d/integration-runtime.sh
. $d/rest.sh
. $d/atlasmap.sh
. $d/verifier.sh
. $d/ui.sh

watch oc get pods
