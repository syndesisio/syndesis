#!/bin/bash
set -eu

# the parent directory of this script
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# the temp directory used, within $DIR
WORK_DIR=$(mktemp -d)

# check if tmp dir was created
if [[ ! "$WORK_DIR" || ! -d "$WORK_DIR" ]]; then
	echo "Could not create temp dir"
	exit 1
fi

# deletes the temp directory
function cleanup() {
	rm -rf "$WORK_DIR"
}

# register the cleanup function to be called on the EXIT signal
trap cleanup EXIT

TARGET_DIR=$WORK_DIR ${DIR}/generator/run.sh

DIFF_FILES=$(diff -q $WORK_DIR $DIR | grep -oE '( |/)syndesis-?[^\n]*\.yml' | grep -oE '[^ /]+' | sort | uniq)

test -z "${DIFF_FILES}" || (printf "Generation validation failed. The following files have not been regenerated:\n\n${DIFF_FILES}\n\nPlease run ./generator/run.sh to re-generate template files before committing\n" && exit 1)
