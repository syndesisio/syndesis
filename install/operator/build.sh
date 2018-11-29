#!/bin/bash
set -e 

echo ===========================================
echo Running go build via a docker build
echo ===========================================
docker build -t syndesis-operator-builder . -f Dockerfile-builder

echo ===========================================
echo Extracting go binary from the builder image
echo ===========================================
docker run syndesis-operator-builder cat /syndesis-operator > syndesis-operator
chmod a+x syndesis-operator
