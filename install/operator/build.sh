#!/bin/bash
#
# Builds the operator, using operator-sdk for developers that have it
# installed locally, or using docker if they don't have it installed.
#
set -e
cd $(dirname "${BASH_SOURCE[0]}")
source "./.lib.sh"

OPERATOR_GO_PACKAGE="github.com/syndesisio/syndesis/install/operator"
OPERATOR_IMAGE_NAME="$(readopt --image-name         syndesis/syndesis-operator)"
S2I_STREAM_NAME="$(readopt     --s2i-stream-name    syndesis-operator)"
OPERATOR_BUILD_MODE="$(readopt --operator-build     auto)"
IMAGE_BUILD_MODE="$(readopt    --image-build        auto)"

if [[ -n "$(readopt --help)" ]] ; then 
	cat <<ENDHELP

usage: ./build.sh [options]

where options are:
  --help                             display this help messages
  --operator-build <auto|docker|go>  how to build the operator executable (default: auto)
  --image-build <auto|docker|s2i>    how to build the image (default: auto)
  --image-name <name>                docker image name (default: syndesis/syndesis-operator)
  --s2i-stream-name <name>           s2i image stream name (default: syndesis-operator)

ENDHELP
	exit 0
fi

#
# TODO Could we avoid copying these files by moving them under the build directory
#
cp "../../app/integration/project-generator/src/main/resources/io/syndesis/integration/project/generator/templates/prometheus-config.yml" "./pkg/generator/assets"

build_operator $OPERATOR_BUILD_MODE $OPERATOR_GO_PACKAGE
build_image $IMAGE_BUILD_MODE $OPERATOR_IMAGE_NAME $S2I_STREAM_NAME

