#!/bin/sh

set -e

root_dir_file=$(dirname `realpath $0`)/root_dir
if [ -f  $root_dir_file ]; then
  root=$(cat $root_dir_file)  
else 
  # Common includes
  pushd `dirname $0`/.. > /dev/null
  root=`pwd`
  popd > /dev/null
fi

function git_pull_upstream {
  git pull upstream master
  set +e
  git rebase upstream/master
  if [ $? != 0 ]; then
    echo "Stashing before rebasing"
    set -e
    git stash
    git rebase upstream/master
    git stash pop
  fi 
  set -e
}

function prepare_dir {
  echo "==========================================================="
  echo "$*"
  echo "==========================================================="
  cd $root/$1
  git_pull_upstream
}

function pod {
    oc get pods -o jsonpath='{.items[*].metadata.name}' -l "component=syndesis-$1"
}
