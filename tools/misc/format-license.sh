#!/bin/env bash

set -eo pipefail

appdir=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../app" && pwd)

cd $appdir && ./mvnw -q -B -N license:format
