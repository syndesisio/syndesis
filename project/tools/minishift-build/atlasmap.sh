#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$BASH_SOURCE")")" && pwd)/vars.sh"

prepare_dir atlasmap
cd runtime
./mvnw clean install -DskipTests
cd runtime
../mvnw clean package fabric8:build -Dfabric8.mode=kubernetes -DskipTests
oc delete pod $(pod atlasmap)
