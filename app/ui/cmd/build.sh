#!/bin/bash
echo ===========================================
echo Running go build via a docker build
echo ===========================================
docker build -t syndesis-ui-builder .
mkdir -p ../target/go/linux-amd64/

echo ===========================================
echo Extracting go binary from the Docker image
echo ===========================================
docker run syndesis-ui-builder cat /syndesis-ui > ../target/go/linux-amd64/syndesis-ui
chmod a+x ../target/go/linux-amd64/syndesis-ui

