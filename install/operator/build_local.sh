#!/bin/bash
#
# This script will help you to rapidly build and push the operator
# image for testing purpouses. The script shoul dbe run from the directory
# it is located.
#
# Usage:
#   ./build_local.sh lgarciaac/my-operator
#    OR
#   ./build_local.sh

set -e 
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
PARENT_DIR=$(dirname $DIR)
DOCKER_IMAGE_TAG=${1:-${user}/syndesis-operator}

echo Generating template from source
cd $PARENT_DIR
./generator/run.sh
cp syndesis.yml syndesis-template.yml

echo Running go build locally
cd $DIR
dep ensure -vendor-only -v
CGO_ENABLED=0 go build -o ./build/syndesis-operator ./cmd/syndesis-operator
chmod a+x ./build/syndesis-operator

echo Baking the binary in an image
cd $PARENT_DIR
cp operator/build/syndesis-operator .
cp operator/Dockerfile .
docker build . -t $DOCKER_IMAGE_TAG && \
  docker push $DOCKER_IMAGE_TAG

echo Cleaning up
rm syndesis-operator
rm Dockerfile
rm syndesis-template.yml

exit 0
