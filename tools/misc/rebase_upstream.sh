#!/bin/bash
# Rebase from syndesisio/syndesis.git $branch to jboss-fuse/syndesis $branch
# if there is a conflict in assets_vfsdata.go use go to regenerate the file

set -o errexit -o pipefail -o nounset

if [ -z "${1}" ] ; then
    echo "ERROR: A branch name parameter is required. Example: 1.12.x"
    exit 1
fi
branch=${1}

if [[ ! $(type -P go) ]]; then
    if [[ ! -x "$GOROOT/bin/go" ]]; then
        printf "ERROR: go is required to regenerate the assets_vfsdata.go, but is not found on \$PATH or installed in \$GOROOT/bin/go.\nEnv \$PATH: %s\n\$GOROOT: %s" "$PATH" "$GOROOT"
        exit 1
    else
        GO="$GOROOT/bin/go"
    fi
else
    GO=go
fi

function is_rebasing {
  if [[ "$(git status | grep -c "rebasing")" == "0" ]]; then
      return 1
  else
      return 0
  fi
}

function is_resolvable_conflict {
    conflict=$(git status --porcelain | grep ^UU)
    if [[ "UU install/operator/pkg/generator/assets_vfsdata.go" == "${conflict}" ]]; then
        return 0
    fi
    return 1
}

function resolve_conflict {
    echo "INFO: Resolving conflict. Regenerating install/operator/pkg/generator/assets_vfsdata.go"
    (cd install/operator || exit 1
    "$GO" generate -x ./pkg/generator/
    git add pkg/generator/assets_vfsdata.go)
}

function rebase_pull_requests() {
  # first, and hopefuly only 100 pull requests
  curl -s "https://${GITHUB_USERNAME}:${GITHUB_ACCESS_TOKEN}@api.github.com/repos/jboss-fuse/syndesis/pulls?per_page=100" | jq -r '.[] | .number,.base.ref,.head.repo.clone_url,.head.ref' | xargs -L 4 | while read -r number base_ref clone_url head_ref; do
    if [ "${base_ref}" != "${branch}" ]; then
      continue
    fi

    local local_branch="pr-${number}"
    echo "INFO: Rebasing pull request #${number}"
    git fetch origin "pull/${number}/head:${local_branch}"
    git checkout "${local_branch}"
    git rebase "origin/${branch}"
    git remote add "pr-${number}-remote" "${clone_url}"
    git push -f "pr-${number}-remote" "HEAD:${head_ref}"
    git remote remove "pr-${number}-remote"
  done
}

# if there is a previous rebase attempt, abort it
is_rebasing && git rebase --abort

git fetch https://github.com/syndesisio/syndesis.git "${branch}:upstream"

echo "INFO: Fetched from upstream to: $(git rev-parse upstream)"

# try rebase see how far we get
git rebase upstream &>/dev/null || true

while is_rebasing; do
    if is_resolvable_conflict; then
      resolve_conflict
    else
        echo "ERROR: Could not rebase. The conflict must be manually resolved."
        echo "$ git status"
        git status
        exit 1
    fi

    # This will override the editor that git uses for message confirmation. true command simply ends with zero exit code.
    # It makes git continue rebase as if user closed interactive editor.
    # otherwise the "continue" op opens an interactive editor
    GIT_EDITOR=true git rebase --continue &> /dev/null
done

git push --force --set-upstream origin "${branch}"

rebase_pull_requests
