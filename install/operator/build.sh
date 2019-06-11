#!/bin/bash
#
# Builds the operator, using operator-sdk for developers that have it
# installed locally, or using docker if they don't have it installed.
#
set -e
cd $(dirname "${BASH_SOURCE[0]}")
source .lib.sh

OPERATOR_GO_PACKAGE="github.com/syndesisio/syndesis/install/operator"

OPERATOR_IMAGE_NAME="$(readopt --image-name         syndesis/syndesis-operator)"
S2I_IMAGE_NAME="$(readopt      --s2i-image-name     syndesis-operator)"
BUILDER_IMAGE_NAME="$(readopt  --builder-image-name syndesis-operator-builder )"

#
# TODO Could we avoid copying these files by moving them under the build directory
#
cp -R "../addons/" "build/conf/addons/"
cp "../../app/integration/project-generator/src/main/resources/io/syndesis/integration/project/generator/templates/prometheus-config.yml" "./pkg/generator/assets"

build_operator "$(readopt --operator-build auto)"
build_image "$(readopt --image-build auto)"

