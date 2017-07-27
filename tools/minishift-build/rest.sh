#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-rest
mvn clean install -Pflash
cd runtime
mvn clean package fabric8:build -Dfabric8.mode=kubernetes -Pflash
oc delete pod $(pod rest)
