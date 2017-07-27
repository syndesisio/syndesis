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
  git rebase upstream/master
}

function prepare_dir {
  echo "==========================================================="
  echo "$*"
  echo "==========================================================="
  cd $root/$1
  git_pull_upstream
}

function pod {
	oc get pod -o name | grep --color=auto --exclude-dir={.bzr,CVS,.git,.hg,.svn} $1 | sed -e "s/^pod\///"
}
