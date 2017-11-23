#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$BASH_SOURCE")")" && pwd)/vars.sh"

prepare_dir syndesis-ui
yarn install
yarn ng build -- --aot --prod
docker build -t syndesis/syndesis-ui -f docker/Dockerfile .
oc delete pod $(pod ui)
