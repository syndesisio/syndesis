#!/bin/env bash
set -euo pipefail

DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
WORK_DIR=$(mktemp -d)
DEP=$(which dep)

# check if tmp dir was created
if [[ ! "${WORK_DIR}" || ! -d "${WORK_DIR}" ]]; then
  echo "Could not create temp dir"
  exit 1
fi

# deletes the temp directory
function cleanup() {
  rm -rf "${WORK_DIR}"
}

# register the cleanup function to be called on the EXIT signal
trap cleanup EXIT

mkdir -p "${WORK_DIR}/src/github.com/syndesisio/syndesis/install/"

ln -s "${DIR}" "${WORK_DIR}/src/github.com/syndesisio/syndesis/install/operator"

cd "${WORK_DIR}/src/github.com/syndesisio/syndesis/install/operator/"
GOPATH="${WORK_DIR}" "${DEP}" check
