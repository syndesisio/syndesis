#!/bin/bash
set -e

project_dir="$1"
version="$2"
target_dir="$3"
local_repo="$4"

if [ -z "$local_repo" ]; then
  local_repo=~/.m2/repository/
fi

mkdir -p "${target_dir}/m2/repo"

echo "Your local maven repo points to $local_repo"
echo "Proceeding to create a maven repo for the docker image based on the local repo."
sed -e "s#file:///tmp/artifacts/m2#file://${local_repo}#g" $target_dir/settings.xml > $target_dir/settings_local.xml
${project_dir}/../mvnw -f $target_dir/m2/project/pom.xml -s $target_dir/settings_local.xml \
  -Dmaven.repo.local=$target_dir/m2/repo package
echo "Cleaning repository metadata to let docker image use it without id repository constraint."
echo "Issue targeted for review in maven 4. See MNG-5185"
find $target_dir/m2/repo/ -name "*.repositories" -delete

cp "${project_dir}/src/main/docker/Dockerfile" $target_dir
