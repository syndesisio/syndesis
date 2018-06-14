#!/bin/bash
set -x

project_dir="$1"
version="$2"
target_dir="$3"
local_repo="$4"

if [ -z "$local_repo" ]; then
  local_repo=~/.m2/repository/
fi

mkdir -p "${target_dir}/m2"
cd ${local_repo}
find ./io/syndesis | grep -F -- "/${version}/" | grep -v -- "-sources\." | grep -v -- "-tests\." | xargs tar -c | tar -vx -C "${target_dir}/m2"

if [ -z "$CAMEL_SNAPSHOT_VERSION" ]; then
   echo "This installation won't use a camel snapshot version"
else
    camel_files=$(find ./org/apache/camel | grep -F -- "$CAMEL_SNAPSHOT_VERSION" | grep -v -- "-sources\." | grep -v -- "-tests\.")
        for file in $camel_files; do
            cp --parents $file "${target_dir}/m2"
        done
fi

cp "${project_dir}/src/main/docker/Dockerfile" $target_dir
