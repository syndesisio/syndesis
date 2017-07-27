#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-verifier
mvn clean install fabric8:build -Dfabric8.mode=kubernetes -PskipTests
oc delete pod $(pod verifier)
