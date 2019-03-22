#!/bin/bash

export PS4='+(${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'
set -x

set -euo pipefail
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

mv -f public/config.json public/config.json.bak || true
cp config.proxy.json public/config.json

sed -i.bu "s#PLACEHOLDER#${BACKEND}#" public/config.json
sed -i.bu "s/Syndesis/Syndesis - DEVELOPMENT/" public/config.json
rm public/config.json.bu