#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-rest
./mvnw clean install -Pflash,deploy -Dfabric8.mode=kubernetes
oc delete pod $(pod rest)
