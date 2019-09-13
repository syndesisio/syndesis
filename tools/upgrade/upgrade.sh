#!/bin/bash

# ===================================================================================================
# Upgrade script, derived from "syndesis"
#
# ==================================================================================================

# Exit if any error occurs
# Fail on a single failed command in a pipeline (if supported)
set -o pipefail

# Fail on error and undefined vars (please don't use global vars, but evaluation of functions for return values)
set -eu

# Save global script args
ARGS=("$@")

# Display a help message.
display_help() {
    cat <<EOT
Syndesis Upgrade Tool

Usage: upgrade.sh [... options ...]

with the following options

    --tag <tag>               Syndesis version/tag to upgrade to. Either --tag or --template is required.
    --backup <dir>            Backup directory to use. The backup is kept after the upgrade
    --migration <dir>         Directory holding the migration scripts
    --local                   Use the local templates stored in this directory instead of
                              fetching them directly from GitHub
    --cleanup                 Whether to cleanup the backup gathered during the upgrade
    --oc-login                oc login command directly copied from openshift console
    --oc-project              When defined, project to log onto right after oc-login
    --wait <seconds>          Wait so many seconds after a sucessful upgrade before exiting
-h  --help                    Display this help message
    --verbose                 Verbose script output (set -x)
EOT
}

run() {
    local basedir=$(basedir)
    local stepsdir="$basedir/steps"

    local tag=$(readopt --tag)
    if [ -z "${tag}" ]; then
          echo "ERROR: --tag is required to specify the target version"
          echo
          display_help
          exit 1
    fi

    # Create backup dir
    local base_backupdir=$(readopt --backup)
    if [ -z "$base_backupdir" ]; then
       base_backupdir=$(mktemp -d)
    fi

    # Login if desired
    oc_login=$(readopt --oc-login)
    if [ -n "${oc_login}" ]; then
        $oc_login --insecure-skip-tls-verify

        oc_project=$(readopt --oc-project)
        if [ -n "${oc_project}" ]; then
		oc project $oc_project
	fi
    else
        # Check whether we have a connection
        set +e
        oc status >/dev/null 2>&1
        if [ $? -ne 0 ]; then
            echo "No connection to OpenShift cluster exists for upgrading"
            echo "Please use --oc-login or connect to the cluster before calling this script"
            exit 1
        fi
        set -e
    fi

    # Switch to project if env variable is set
    if [ -n "${SYNDESIS_UPGRADE_PROJECT:-}" ]; then
        oc project "${SYNDESIS_UPGRADE_PROJECT}"
    fi

    local time=$(date +"%Y-%m-%d-%s")
    local top_dir="${base_backupdir}/$time"
    [ -d ${top_dir} ] || mkdir -p ${top_dir}

    # Create backup dir
    local backupdir="${top_dir}/backup"
    [ -d ${backupdir} ] || mkdir -p ${backupdir}

    # Create target dir
    local workdir="${top_dir}/target"
    [ -d $workdir ] || mkdir -p ${workdir}

    # Check migrationdir
    local migrationdir="$(readopt --migration)"
    echo $time > ${base_backupdir}/LATEST

    # Redirect sript output to top_dir for later reference
    exec > >(tee -i ${top_dir}/upgrade.log)
    exec 2>&1

    # Preflight check
    source $basedir/migration/preflight.sh
    local current_tag=$(read_global_config syndesis)
    preflight_version_check "$current_tag" "$tag"

    # Copy the migration jar to the workdir for later reference
    cp $basedir/syndesis-cli.jar $workdir/

    echo "============================================="
    echo "=== STARTING UPGRADE TO SYNDESIS $tag "
    echo "============================================="
    echo

    perform_actions "$backupdir" \
                    "$workdir" \
                    "$current_tag" \
                    "$tag" \
                    "$migrationdir" \
                    "$(ls $stepsdir/prep_* $stepsdir/upgrade_*)"

    local wait_secs=$(readopt --wait)
    if [ -n "${wait_secs}" ]; then
        echo "Sleeping for ${wait_secs} seconds"
        sleep ${wait_secs}
    fi
}

perform_actions() {
    local backupdir=${1}
    local workdir=${2}
    local current_tag=${3}
    local tag=${4}
    local migrationdir=${5}
    local steps=${6}

    local actions_performed=""

    # Prepare for an upgrade.
    for step_def in $steps; do
        source $step_def
        local step="$(extract_step $step_def)"
        local label=$(eval "${step}::label")
        echo "=== * $label ($(basename $step_def))"
        set +e
        actions_performed="$step $actions_performed"
        (eval "set -e; ${step}::run \"$backupdir\" \"$workdir\" \"$tag\" \"$migrationdir\"")
        if [ $? -ne 0 ]; then
            set -e
            echo "====> Error ==> Rollback "
            rollback $backupdir $current_tag $actions_performed
            cleanup_backupdir $backupdir
            exit 1
        fi
        set -e
    done
}

rollback() {
    local backupdir=$1
    local current_tag=$2
    local errors=""
    shift 2
    echo
    echo "----- Rollback"
    local cleanup=$(hasflag --cleanup)
    for step in $@; do
        local label=$(eval "${step}::label")
        echo "--- * Rolling back '$label'"
        set +e
        (eval "${step}::rollback \"$backupdir\" \"$workdir\" \"$cleanup\" \"$current_tag\"")
        if [ $? -ne 0 ]; then
            set -e
            if [ $(hasflag --stop-on-rollback-error) ]; then
                echo "====> Rollback Error ==> Exit"
                echo "Backup directory *not* deleted: $backupdir"
                exit 1
            else
                echo "====> Rollback Error !!"
                echo "====> Continuing with rollback (specify --stop-on-rollback-error to stop here)"
                errors="${errors:-}  * ${step}\n"
            fi
        fi
        set -e
    done
    if [ -n "${errors}" ]; then
      cat <<EOT
==========================================
!!!!!!!!! Errors during Rollback !!!!!!!!!

The following rollback compensation steps
caused an error

$errors

The setup is very likely broken now and you
have to manually fix it.
Please check the log output above for any
detailed error messages.
==========================================
EOT
    fi
}

# Dir where this script is located
basedir() {
    # Default is current directory
    local script=${BASH_SOURCE[0]}

    # Resolve symbolic links
    if [ -L $script ]; then
        if readlink -f $script >/dev/null 2>&1; then
            script=$(readlink -f $script)
        elif readlink $script >/dev/null 2>&1; then
            script=$(readlink $script)
        elif realpath $script >/dev/null 2>&1; then
            script=$(realpath $script)
        else
            echo "ERROR: Cannot resolve symbolic link $script"
            exit 1
        fi
    fi

    local dir=$(dirname "$script")
    local full_dir=$(cd "${dir}" && pwd)
    echo ${full_dir}
}

cleanup_backupdir() {
    local backupdir="$1"
    if [ -z "$(readopt --backup)" ]; then
        rm -rf $backupdir
    fi
}

extract_step() {
  local path=${1}
  echo $(basename $path) | sed -e "s/.*_[0-9]*_//"
}

extract_version_from_template() {
  local template=$(template_path)

  if [ ! -f $template ]; then
      echo "ERROR: No template $template found"
      return
  fi

  if [ $(grep 'syndesis:\s*"' $template | wc -l) != 1 ]; then
      echo "ERROR: Could not extract version from $template"
      return
  fi

  grep 'syndesis:\s*"' $template | sed -e 's/.*"\([^"]*\)".*/\1/'
}

template_path() {
#    echo $(basedir)/syndesis.yml
    local template=$(readopt --template)
    if [ -n "${template}" ]; then
      [ "${template}" != "${template#/}" ] && echo ${template} || echo $(basedir)/${template}
      return
    fi
    echo $(basedir)/../../install/syndesis.yml
}

# =================================================================================
# Startup

# Include common utilities
source $(basedir)/common.sh

if [ $(hasflag --verbose) ]; then
    export PS4='+(${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'
    set -x
fi

run "$@"
