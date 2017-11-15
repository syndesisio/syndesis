#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$0")")" && pwd)/vars.sh"

prepare_dir syndesis-integration-runtime
./mvnw clean install -DskipTests
