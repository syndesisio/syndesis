#!/bin/sh

if [ -n "${LOADER_HOME:-}" ] && [ $(ls ${LOADER_HOME}/*.jar 2>/dev/null | wc -l) -gt 0 ]; then
  LOADER_PATH=""
  for jar in ${LOADER_HOME}/*.jar; do
    filename=$(basename $jar)
    LOADER_PATH="$LOADER_PATH,$filename"
  done
  LOADER_PATH=$(echo $LOADER_PATH | cut -c 2-)
  export LOADER_PATH
  echo "Using the following extensions in the spring-boot loader path: $LOADER_PATH"
else
  echo "No extensions found"
fi

