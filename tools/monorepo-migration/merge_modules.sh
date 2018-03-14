#!/bin/bash

set -euo pipefail

# Script for checking out "syndesis" and add all repos into the module
# This is a one-shot script, to be run only once.
# It works directly on the upstream repo, so be carefule

# Repos map: key == dir name, value == GitHub repo name
repos=(
  "rest:syndesis-rest"
  "ui:syndesis-ui"
  "project:syndesis-project"
  "verifier:syndesis-verifier"
  "ux:syndesis-ux"
  "tests:syndesis-system-tests"
  "connectors:connectors"
  "runtime:syndesis-integration-runtime"
  "s2i:syndesis-s2i-image"
  "deploy:syndesis-openshift-templates"
  )

# Clone syndesis in a fresh repository
git clone git@github.com:syndesisio/syndesis.git
cd syndesis

# Remove all submodules
for repo in "${repos[@]}" ; do
    dir="${repo%%:*}"
    [ -d $dir ] && rm -rf $dir
done
git commit -m "Removed git submodules" -a

# Merge an individual project
merge_project() {
  project=$1
  repo=$2

  echo "====================================================================="
  echo "Processing $project : $repo"
  echo "====================================================================="

  git remote add $project $repo
  git fetch $project

  git checkout -B ${project}-master $project/master

  [ -d $project ] && git mv $project ${project}.orig
  mkdir $project
  git mv $(ls -A . | grep -v -e "^${project}\(.orig\)*\$" | grep -v -e '^.git$') $project
  [ -d ${project}.orig ] && git mv ${project}.orig ${project}/${project}

	git commit -am "Move files from $repo into the $project subdirectory"

  git checkout master
  git merge ${project}-master --allow-unrelated-histories -m "Merging in $repo"

  git branch -D ${project}-master
  git remote rm $project
}

# Checkout individual repos in subdirs and merge to master
for repo in "${repos[@]}" ; do
    project="${repo%%:*}"
    repo_name="${repo##*:}"
    merge_project $project "https://github.com/syndesisio/${repo_name}.git"
done
