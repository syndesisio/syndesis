#!/bin/bash

VERSION="$1"
TARGET_DIR="$2"

mkdir -p "$TARGET_DIR/.m2/repository"
cd ~/.m2/repository
find ./io/syndesis | grep -F -- "/${VERSION}/" | xargs tar -c | tar -x -C "$TARGET_DIR/.m2/repository"

