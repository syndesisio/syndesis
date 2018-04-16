#!/bin/bash

# Create image syndesis/syndesis-upgrade:$tag for doing an upgrade
# to this tag
tag=${1:-latest}

set -e

# Default is current directory
cd $(dirname "${BASH_SOURCE[0]}")

# Copy syndesis.yml
cp ../../install/syndesis.yml .

docker build -t syndesis/syndesis-upgrade:${tag} .
