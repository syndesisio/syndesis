#!/bin/bash
set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
$dir/templates/run.sh

docker build $dir/centos -t syndesis/syndesis-builder

