#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-ui
yarn install
yarn ng build -- --aot --prod
docker build -t syndesis/syndesis-ui .
oc delete pod $(pod syndesis-ui)
