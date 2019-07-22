#!/bin/bash
#set -x

project_dir="$1"
version="$2"
target_dir="$3"
local_repo="$4"
syndesis_dependencies="/tmp/syndesis-dependencies"

if [ -z "$local_repo" ]; then
  local_repo=~/.m2/repository/
fi

mkdir -p "${target_dir}/m2"
mkdir -p "${target_dir}/m2.cache"
cd ${local_repo}

# Store a file with the list of Syndesis local dependencies
find ./io/syndesis | grep -F -- "/${version}/" | grep -v -- "-sources\." | grep -v -- "-tests\." > $syndesis_dependencies
echo "Found following list of local Syndesis ${version} dependencies"
cat /tmp/syndesis-dependencies | grep .pom | grep -v .lastUpdated | sed -e 's/\/[^\/]*$//g' -e 's/\.\///g'

if [[ ! "$(ls -A ${target_dir}/m2.cache)" ]]; then
    echo "Initializing local maven cache ${target_dir}/m2.cache"
    echo "Installing all base project dependencies..."
    mvn -f $target_dir/m2/project/pom.xml -s $target_dir/settings.xml dependency:copy-dependencies \
      -DoutputDirectory=${target_dir}/m2.cache -Dmdep.copyPom=true -Dmdep.useRepositoryLayout=true

    echo "Going to cache locally Syndesis ${version} dependencies."
    cat $syndesis_dependencies | xargs tar -c | tar -vx -C "${target_dir}/m2.cache" > /dev/null
else
    echo "Using cached base project dependencies. Clean target directory if you need to refresh the cache though it will \
      take longer to build your S2I Docker image."
fi

echo "Installing Syndesis ${version} dependencies to target directory..."
cat $syndesis_dependencies | xargs tar -c | tar -vx -C "${target_dir}/m2" > /dev/null

if [ -z "$CAMEL_SNAPSHOT_VERSION" ]; then
   echo "This installation won't use a camel snapshot version"
else
   camel_files=$(find ./org/apache/camel | grep -F -- "$CAMEL_SNAPSHOT_VERSION" | grep -v -- "-sources\." | grep -v -- "-tests\.")
   for file in $camel_files; do
      cp --parents $file "${target_dir}/m2"
   done
fi

cp "${project_dir}/src/main/docker/Dockerfile" $target_dir
