#!/usr/bin/env bash

. "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/vars.sh"

prepare_dir syndesis-rest
./mvnw clean install -Ddeploy -Dfabric8.mode=kubernetes
oc delete pod $(pod rest)
