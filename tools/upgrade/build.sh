#!/bin/bash

# Create image syndesis/syndesis-upgrade:$tag for doing an upgrade
# to this tag
tag=${1:-latest}
image=${2:-syndesis/syndesis-upgrade}

set -e

# Default is current directory
cd $(dirname "${BASH_SOURCE[0]}")

# Copy syndesis-cli.jar
cp ../../app/server/cli/target/syndesis-cli.jar .

echo "Buidling syndesis/syndesis-upgrade:${tag} image"
docker build --build-arg version=${tag} -t ${image}:${tag} .
