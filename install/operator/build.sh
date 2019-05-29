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
    echo 'operator-sdk command not found, will build with docker.'
    USE_DOCKER_BUILD=true
elif [[ "$(which go)" == "" ]] ; then
    echo 'go command not found, will build with docker.'
    USE_DOCKER_BUILD=true
elif [[ "$DIR" != "$GOPATH/src/github.com/syndesisio/syndesis/install/operator" ]] ; then
    echo 'Project not checked out into the $GOPATH, will build with docker.'
    echo ''
    echo 'Next time try checking out like this instead:'
    echo '    mkdir -p $GOPATH/src/github.com/syndesisio'
    echo '    git clone https://github.com/syndesisio/syndesis.git'
    echo '    syndesis/install/operator/build.sh'
    USE_DOCKER_BUILD=true
fi

#
# TODO Could we avoid copying these files by moving them under the build directory
#
cp "$DIR/../syndesis.yml" "$DIR/build/conf/syndesis-template.yml"
cp -R "$DIR/../addons/" "$DIR/build/conf/addons/"

if [[ "$USE_DOCKER_BUILD" == "true" ]] ; then

    echo ======================================================
    echo operator-sdk not found. Running build in Docker
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
    operator-sdk generate k8s
    # operator-sdk generate openapi
    operator-sdk build "${OPERATOR_IMAGE_NAME}"

fi

echo ======================================================
echo "Operator Image Built: ${OPERATOR_IMAGE_NAME}"
echo ======================================================

