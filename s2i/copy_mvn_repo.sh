#!/bin/bash

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VERSION="$1"
TARGET_DIR="$2"

mkdir -p "$TARGET_DIR/repo"
cd ~/.m2/repository
find ./io/syndesis | grep -F -- "/${VERSION}/" | xargs tar -c | tar -x -C "$TARGET_DIR/repo"

cp "${PROJECT_DIR}/src/main/docker/Dockerfile" $TARGET_DIR

