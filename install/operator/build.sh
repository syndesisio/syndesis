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
OPERATOR_IMAGE_TAG="$(readopt --image-tag           latest)"
S2I_STREAM_NAME="$(readopt     --s2i-stream-name    syndesis-operator)"
OPERATOR_BUILD_MODE="$(readopt --operator-build     auto)"
IMAGE_BUILD_MODE="$(readopt    --image-build        auto)"
GO_BUILD_OPTIONS="$(readopt    --go-options         '')"

if [[ -n "$(readopt --help)" ]] ; then
	cat <<ENDHELP

usage: ./build.sh [options]

where options are:
  --help                                  display this help messages
  --operator-build <auto|docker|go|skip>  how to build the operator executable (default: auto)
  --image-build <auto|docker|s2i|skip>    how to build the image (default: auto)
  --image-name <name>                     docker image name (default: syndesis/syndesis-operator)
  --image-tag  <tag>                      docker image tag (default: latest)
  --s2i-stream-name <name>                s2i image stream name (default: syndesis-operator)
  --go-options <name>                     additional build options to pass to the go build

ENDHELP
	exit 0
fi

if [ $OPERATOR_BUILD_MODE != "skip" ] ; then
  build_operator $OPERATOR_BUILD_MODE -ldflags "-X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorImage=$OPERATOR_IMAGE_NAME -X github.com/syndesisio/syndesis/install/operator/pkg.DefaultOperatorTag=$OPERATOR_IMAGE_TAG" $GO_BUILD_OPTIONS
fi

if [ $IMAGE_BUILD_MODE != "skip" ] ; then
  build_image $IMAGE_BUILD_MODE $OPERATOR_IMAGE_NAME $OPERATOR_IMAGE_TAG $S2I_STREAM_NAME
fi
