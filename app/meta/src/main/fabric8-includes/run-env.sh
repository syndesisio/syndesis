#!/bin/sh

if ls $LOADER_HOME | grep -e ".*\.jar$" > /dev/null 2>&1; then
    EXTENSION_JARS=$(ls $LOADER_HOME | grep -e ".*\.jar$")
    export LOADER_PATH=$(echo $EXTENSION_JARS | sed 's/ /,/g')
fi

echo "Using the following extensions in the spring-boot loader path: $LOADER_PATH"
