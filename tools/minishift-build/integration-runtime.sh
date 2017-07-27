#!/bin/sh

. $(dirname `realpath $0`)/vars.sh

prepare_dir syndesis-integration-runtime
mvn clean install
