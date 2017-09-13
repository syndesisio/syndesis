#!/usr/bin/env bash

. "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/vars.sh"

prepare_dir syndesis-integration-runtime
./mvnw clean install -DskipTests
