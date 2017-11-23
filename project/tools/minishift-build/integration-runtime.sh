#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$BASH_SOURCE")")" && pwd)/vars.sh"

prepare_dir syndesis-integration-runtime
./mvnw clean install -DskipTests
