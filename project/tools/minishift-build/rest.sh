#!/usr/bin/env bash

. "$(cd "$(dirname "$(readlink -f "$0")")" && pwd)/vars.sh"

prepare_dir syndesis-rest
./mvnw clean install -Ddeploy
oc delete pod $(pod rest)
