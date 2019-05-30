#!/bin/bash
#
# Builds the operator, using operator-sdk for developers that have it
# installed locally, or using docker if they don't have it installed.
#
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
OPERATOR_IMAGE_NAME="syndesis/syndesis-operator"
BUILDER_IMAGE_NAME="syndesis-operator-builder"

set -e

if [[ "$(which docker)" == "" ]] ; then
    echo 'docker command not found.  Required to build this operator'
    exit 1
fi

USE_DOCKER_BUILD=false

if [[ "$(which operator-sdk)" == "" ]] ; then
    USE_DOCKER_BUILD=true
    echo 'operator-sdk command not found, will build with docker.'
    echo ''
elif [[ "$(which go)" == "" ]] ; then
    USE_DOCKER_BUILD=true
    echo 'go command not found, will build with docker.'
    echo ''
elif [[ "$DIR" != "$GOPATH/src/github.com/syndesisio/syndesis/install/operator" ]] ; then
    USE_DOCKER_BUILD=true
    echo 'Project not checked out into the $GOPATH, will build with docker.'
    echo ''
    echo 'Next time checkout the project to the $GOPATH instead:'
    echo '    mkdir -p $GOPATH/src/github.com/syndesisio'
    echo '    git clone https://github.com/syndesisio/syndesis.git'
    echo ''
fi

#
# TODO Could we avoid copying these files by moving them under the build directory
#
cp -R "$DIR/../addons/" "$DIR/build/conf/addons/"
cp "$DIR/../../app/integration/project-generator/src/main/resources/io/syndesis/integration/project/generator/templates/prometheus-config.yml" "$DIR/pkg/generator/assets"

if [[ "$USE_DOCKER_BUILD" == "true" ]] ; then

    echo ======================================================
    echo Running build in Docker
    echo ======================================================
    rm -rf build/_output
    docker build -t "${BUILDER_IMAGE_NAME}" .

    echo ======================================================
    echo Downloading built operator binary from Docker build
    echo ======================================================
    mkdir -p ./build/_output/bin
    docker run "${BUILDER_IMAGE_NAME}" cat /operator > ./build/_output/bin/operator
    chmod a+x ./build/_output/bin/operator

    echo ======================================================
    echo Building Operator Image
    echo ======================================================
    docker build -f "build/Dockerfile" -t "${OPERATOR_IMAGE_NAME}" .

else

    export GO111MODULE=on
    go mod vendor
    go generate ./pkg/...
    operator-sdk generate k8s
    # operator-sdk generate openapi
    go test ./cmd/... ./pkg/...
    operator-sdk build "${OPERATOR_IMAGE_NAME}"

fi

echo ======================================================
echo "Operator Image Built: ${OPERATOR_IMAGE_NAME}"
echo ======================================================

