#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-integration-runtime
./mvnw clean install -DskipTests
