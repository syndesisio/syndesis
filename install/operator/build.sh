#!/bin/bash
#
# Builds the operator, using operator-sdk for developers that have it
# installed locally, or using docker if they don't have it installed.
#

set -e
cd -P $(dirname "${BASH_SOURCE[0]}")

source "$(pwd)/../../tools/bin/commands/util/common_funcs"
source "$(pwd)/../../tools/bin/commands/util/operator_funcs"
source "$(pwd)/../../tools/bin/commands/util/openshift_funcs"
source "./.lib.sh"

OPERATOR_IMAGE_NAME="$(readopt --image-name         docker.io/syndesis/syndesis-operator)"
OPERATOR_IMAGE_TAG="$(readopt  --image-tag          latest)"
S2I_STREAM_NAME="$(readopt     --s2i-stream-name    syndesis-operator)"
OPERATOR_BUILD_MODE="$(readopt --operator-build     auto)"
IMAGE_BUILD_MODE="$(readopt    --image-build        auto)"
SOURCE_GEN="$(readopt          --source-gen         on)"
GO_BUILD_OPTIONS="$(readopt    --go-options         '')"

if [[ -n "$(readopt --help)" ]] ; then
	cat <<ENDHELP

usage: ./build.sh [options]

where options are:
  --help                                  display this help messages
  --source-gen <on|skip|verify-none>      should the source generators be run (default: on)
  --operator-build <auto|docker|go|skip>  how to build the operator executable (default: auto)
  --image-build <auto|docker|s2i|skip>    how to build the image (default: auto)
  --image-name <name>                     docker image name (default: syndesis/syndesis-operator)
  --image-tag  <tag>                      docker image tag (default: latest)
  --s2i-stream-name <name>                s2i image stream name (default: syndesis-operator)
  --go-options <name>                     additional build options to pass to the go build

ENDHELP
	exit 0
fi

#
# Timestamp for the building of the operator
#
BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%z)

if [ $OPERATOR_BUILD_MODE != "skip" ] ; then
	LD_FLAGS=$(echo "-X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorImage=${OPERATOR_IMAGE_NAME}" \
		"-X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorTag=${OPERATOR_IMAGE_TAG}" \
		"-X github.com/syndesisio/syndesis/install/operator/pkg.BuildDateTime=${BUILD_TIME}")
	echo "LD_FLAGS: ${LD_FLAGS}"
  build_operator $OPERATOR_BUILD_MODE "$SOURCE_GEN" -ldflags "${LD_FLAGS}" $GO_BUILD_OPTIONS
fi

if [ $IMAGE_BUILD_MODE != "skip" ] ; then
  build_image $IMAGE_BUILD_MODE $OPERATOR_IMAGE_NAME $OPERATOR_IMAGE_TAG $S2I_STREAM_NAME
fi
