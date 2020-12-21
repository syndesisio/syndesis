#!/bin/bash
# Rebase from syndesisio/syndesis.git 1.12.x to jboss-fuse/syndesis 1.12.x
# if there is a conflict in assets_vfsdata.go use go to regenerate the file

if [ -z ${1} ] ; then
    echo "Error: A branch name parameter is required. Example: 1.12.x"
    exit 1
fi
branch=${1}

function conflicts {
    conflict=$(git status --porcelain | grep ^UU)
    assets_vfsdata_conflict_only=false
    if [[ "UU install/operator/pkg/generator/assets_vfsdata.go" == ${conflict} ]]; then
        assets_vfsdata_conflict_only=true
    fi
    echo ${assets_vfsdata_conflict_only}
}

git fetch https://github.com/syndesisio/syndesis.git ${branch}:upstream
# if there is a previous rebase attempt, abort it
[[ -d .git/rebase-apply/ ]] && git rebase --abort
git rebase upstream &>/dev/null
if [ $? -ne 0 ]; then
    assets_vfsdata_conflict_only=$(conflicts)
    echo ">> assets_vfsdata_conflict_only: ${assets_vfsdata_conflict_only}"
    while [ "${assets_vfsdata_conflict_only}" == "true" ]; do
        if [[ ! -f "$GOROOT/bin/go" ]]; then
            echo "ERROR: go is required to regenerate the assets_vfsdata.go, but is not installed in \$GOROOT/bin/go. Env \$GOROOT: $GOROOT"
            exit 1
        fi
        echo "--> Resolve conflict. Regenerate install/operator/pkg/generator/assets_vfsdata.go"
        cd install/operator
        $GOROOT/bin/go generate -x ./pkg/generator/
        git add pkg/generator/assets_vfsdata.go
        cd ../..
        # This will override the editor that git uses for message confirmation. true command simply ends with zero exit code.
        # It makes git continue rebase as if user closed interactive editor.
        # otherwise the "continue" op opens an interactive editor
        GIT_EDITOR=true git rebase --continue
        assets_vfsdata_conflict_only=$(conflicts)
        echo "  >> assets_vfsdata_conflict_only: ${assets_vfsdata_conflict_only}"
    done
    if [[ "${assets_vfsdata_conflict_only}" == "false" ]]; then
        echo "Could not rebase. The conflict must be manually resolved."
        git status
        exit 1
    fi
fi
if [[ -d .git/rebase-apply/ ]]; then
    echo "ERROR: There are pending git conflicts to resolve."
    exit 1
else
    git push --force --set-upstream origin ${branch}
fi
