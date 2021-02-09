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
