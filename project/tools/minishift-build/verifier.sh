#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$BASH_SOURCE")")" && pwd)/vars.sh"

prepare_dir syndesis-verifier
./mvnw clean install fabric8:build -Dfabric8.mode=kubernetes -PskipTests
oc delete pod $(pod verifier)
