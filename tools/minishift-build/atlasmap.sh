#!/usr/bin/env bash

. "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/vars.sh"

prepare_dir atlasmap
cd runtime
./mvnw clean install -DskipTests
cd runtime
../mvnw clean package fabric8:build -Dfabric8.mode=kubernetes -DskipTests
oc delete pod $(pod atlasmap)
