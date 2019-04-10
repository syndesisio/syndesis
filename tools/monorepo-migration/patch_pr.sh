#!/bin/sh

set -euo pipefail

# Assumption: Being in toplevel "syndesis" workingdir
# Usage:
# patch_pr.sh <module dir> <patch nr>
#
# This will create a branch "pr/<patch nr>" which contains the merged PR

# Repos map: key == dir name, value == GitHub repo name
REPOS=(
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

if [ -d syndesis ]; then
  cd syndesis
fi

get_pr_url() {
  local key="$1"
  local pr="$2"
  for repo in "${REPOS[@]}" ; do
    project="${repo%%:*}"
    if [ "$project" = "$key" ]; then
      repo_name="${repo##*:}"
      echo "https://github.com/syndesisio/${repo_name}/pull/${pr}"
    fi
  done
}

module_dir=$1
pr=$2

if [ ! -d $module_dir ]; then
  echo "No module directory $module_dir found"
  exit 1;
fi

url=$(get_pr_url $module_dir $pr)
if [ -z "$url" ]; then
  echo "Usage: $0 <module dir> <patch nr>"
fi

patch_file=/tmp/pr_${module_dir}_${pr}.patch
curl -L $url.patch > $patch_file
cd $module_dir
git checkout -b "pr/${module_dir}-$pr"
patch -p1 < $patch_file
git status -s | grep -v -e '^ M ' | grep -v -e '^ D ' | sed -e 's/^?? //' | xargs git add
git commit -a -m "Applied PR $url (Module: $module_dir, PR: $pr)"

cat - <<EOT


============================================================================


PR $pr applied successfully.

The patch has been committed locally to branch pr/${module_dir}-$pr
Please push this branch to your fork and create a new PR against Syndesis
(possibly copying over relevant comments from the original PR)
EOT
