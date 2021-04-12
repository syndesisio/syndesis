#!/bin/bash
#
# Builds the operator, using operator-sdk for developers that have it
# installed locally, or using docker|podman if they don't have it installed.
#

set -e
cd -P $(dirname "${BASH_SOURCE[0]}")

source "$(pwd)/../../tools/bin/commands/util/common_funcs"
source "$(pwd)/../../tools/bin/commands/util/operator_funcs"
source "$(pwd)/../../tools/bin/commands/util/openshift_funcs"
source "$(pwd)/../../tools/bin/commands/util/kube_funcs"
source "./.lib.sh"

#
# Ensure any errors from check_error are printed to the terminal
# Uses same trap as syndesis command
#
#
ERROR_FILE="$(mktemp /tmp/syndesis-build-output.XXXXXX)"
add_to_trap "print_error ${ERROR_FILE}"
#
# This should be the only trap
# Other instances should use add_to_trap
#
trap "process_trap" EXIT

export IMAGE_NAME="syndesis-operator"

export CONTAINER_REGISTRY="$(readopt  --registry           docker.io)"
export IMAGE_NAMESPACE="$(readopt     --image-namespace    syndesis)"
export IMAGE_TAG="$(readopt           --image-tag          latest)"
S2I_STREAM_NAME="$(readopt     --s2i-stream-name    syndesis-operator)"
OPERATOR_BUILD_MODE="$(readopt --operator-build     auto)"
IMAGE_BUILD_MODE="$(readopt    --image-build        auto)"
SOURCE_GEN="$(readopt          --source-gen         on)"
export GO_BUILD_OPTIONS="$(readopt    --go-options         '')"
GO_PROXY_URL="$(readopt        --go-proxy           https://proxy.golang.org)"

if [[ -n "$(readopt --help)" ]] ; then
	cat <<ENDHELP

usage: ./build.sh [options]

where options are:
  --help                                  display this help messages
  --source-gen <on|skip|verify-none>      should the source generators be run (default: on)
  --operator-build <auto|docker|podman|go|skip>  how to build the operator executable (default: auto)
  --image-build <auto|docker|podman|s2i|skip>    how to build the image (default: auto)
  --registry <registry host[:port]>       custom container registry to locate the image
  --image-namespace <namespace>           container namespace of the image (default: syndesis)
  --image-tag  <tag>                      container image tag (default: latest)
  --s2i-stream-name <name>                s2i image stream name (default: syndesis-operator)
  --go-options <name>                     additional build options to pass to the go build
  --go-proxy <url>                        proxy url for finding go dependencies (default: https://proxy.golang.org)

ENDHELP
	exit 0
fi

#
# Timestamp for the building of the operator
#
export BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%z)

if [ $OPERATOR_BUILD_MODE != "skip" ] ; then
  build_operator $OPERATOR_BUILD_MODE "$SOURCE_GEN" "$GO_PROXY_URL"
fi

if [ $IMAGE_BUILD_MODE != "skip" ] ; then
  build_image $IMAGE_BUILD_MODE $S2I_STREAM_NAME
fi
