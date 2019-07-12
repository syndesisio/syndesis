#!/bin/bash
#set -x

project_previous="$1"
project_actual="$2"
mvn_repo="$3"

if [ -z "$project_previous" ] || [ -z "$project_actual" ] || [ -z "mvn_repo" ] ; then
  echo "ERROR: please provide mvn dependency list file for two project and the local maven repo directory."
  exit 1
fi

echo "Going to retrieve dependency difference between cached project and base project and remove the ones not used by the latter"

sed -e "s/\[INFO\]    //g" -e "s/:[^:]*$//g" -n -e "/following files/,/BUILD SUCCESS/p" $project_previous | tail -n +2 | head -n -3 > "$project_previous-list"
sed -e "s/\[INFO\]    //g" -e "s/:[^:]*$//g" -n -e "/following files/,/BUILD SUCCESS/p" $project_actual | tail -n +2 | head -n -3 > "$project_actual-list"

old_dependencies=$(diff "$project_previous-list" "$project_actual-list" | grep "<" | sed -e "s/< //g")
for dependency in $old_dependencies; do
  mvn_dependency_dir=$mvn_repo/$(echo "$dependency" | awk -F':' '{gsub("\\\\.","/",$1);print $1"/"$2"/"$4}')
  echo "Deleting $mvn_dependency_dir"
  rm -rf $mvn_dependency_dir
done