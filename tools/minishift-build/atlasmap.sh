#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir atlasmap
./mvnw clean install -DskipTests
cd runtime
../mvnw clean package fabric8:build -Dfabric8.mode=kubernetes -DskipTests
oc delete pod $(pod atlasmap)
