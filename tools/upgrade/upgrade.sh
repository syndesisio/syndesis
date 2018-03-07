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
    --template <template>     Path to a local target template.
    --backup <dir>            Backup directory to use. The backup is kept after the upgrade
    --migration <dir>         Directory holding the migration scripts
    --local                   Use the local templates stored in this directory instead of
                              fetching them directly from GitHub
    --cleanup                 Whether to cleanup the backup gathered during the upgrade
    --oc-login                oc login command directly copied from openshift console
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
      local template=$(readopt --template)
      if [ -n "${template}" ]; then
          tag=$(extract_version_from_template ${template})
          check_error $tag
      else
          echo "ERROR: --tag or --template is required to specify the target version"
          echo
          display_help
          exit 1
      fi
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
    fi

    # Switch to project if env variable is set
    if [ -n "${SYNDESIS_UPGRADE_PROJECT:-}" ]; then
        oc project "${SYNDESIS_UPGRADE_PROJECT}"
    fi

    local time=$(date +"%Y-%m-%d-%s")
    local backupdir=${base_backupdir}/$time
    [ -d ${backupdir} ] || mkdir -p ${backupdir}

    # Create a workdir
    local workdir=${base_backupdir}/workdir
    [ -d $workdir ] || mkdir -p ${workdir}

    # Check migrationdir
    local migrationdir=$(readopt --migration)
    echo $time > ${base_backupdir}/LATEST

    echo "============================================="
    echo "===== STARTING UPGRADE TO SYNDESIS $tag "
    echo "============================================="
    echo

    local actions_performed=""
    # Prepare for an upgrade.
    for step_def in $(ls $stepsdir/prep_* $stepsdir/upgrade_*); do
        source $step_def
        local step="$(extract_step $step_def)"
        local label=$(eval "${step}::label")
        echo "=== * $label ($(basename $step_def))"
        set +e
        (eval "set -e; ${step}::run $backupdir $workdir $tag $migrationdir")
        if [ $? -ne 0 ]; then
            set -e
            echo "====> Error ==> Rollback "
            rollback $backupdir $actions_performed
            cleanup_backupdir $backupdir
            exit 1
        fi
        actions_performed="$step $actions_performed"
        set -e
    done

    local wait_secs=$(readopt --wait)
    if [ -n "${wait_secs}" ]; then
        echo "Sleeping for ${wait_secs} seconds"
        sleep ${wait_secs}
    fi
}

rollback() {
    local backupdir=$1
    shift 1
    echo
    echo "----- Rollback"
    local cleanup=$(hasflag --cleanup)
    for step in $@; do
      local label=$(eval "${step}::label")
      echo "--- * Rolling back '$label'"
      set +e
      (eval "${step}::rollback $backupdir $workdir $cleanup")
      if [ $? -ne 0 ]; then
        set -e
        echo "====> Rollback Error ==> Exit"
        echo "Backup directory *not* deleted: $backupdir"
        exit 1
      fi
      set -e
    done
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

# Checks if a flag is present in the arguments.
hasflag() {
    filters="$@"
    for var in "${ARGS[@]}"; do
        for filter in $filters; do
          if [ "$var" = "$filter" ]; then
              echo 'true'
              return
          fi
        done
    done
}

# Read the value of an option.
readopt() {
    filters="$@"
    next=false
    for var in "${ARGS[@]}"; do
        if $next; then
            echo $var
            break;
        fi
        for filter in $filters; do
            if [[ "$var" = ${filter}* ]]; then
                local value="${var//${filter}=/}"
                if [ "$value" != "$var" ]; then
                    echo $value
                    return
                fi
                next=true
            fi
        done
    done
}


check_error() {
    local msg="$*"
    if [ "${msg//ERROR/}" != "${msg}" ]; then
        echo $msg
        exit 1
    fi
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
# ===================================================================================================
# Postgres funcs

pg_backup() {
    local backupdir=${1}
    local db=${2}

    local pod=$(pod "syndesis-db")
    check_error $pod

    if [ ! -d ${backupdir} ]; then
      mkdir -p ${backupdir}
    fi

    # Create backup file remotely
    oc rsh $pod bash -c "pg_dump -Fc -b $db | base64" > ${backupdir}/${db}
}

pg_restore() {
    local backupdir=${1}
    local db=${2}

    local pod=$(pod "syndesis-db")
    check_error $pod

    # Create backup file remotely
    if [ ! -f "${backupdir}/${db}" ]; then
        echo "ERROR: No backup for ${db} found in ${backupdir}"
        exit 1
    fi

    local cmd=$(cat <<EOT
set -e;
base64 -d -i > /var/lib/pgsql/data/${db}.dmp;
createdb -T template0 ${db}_restore;
pg_restore -v -d ${db}_restore /var/lib/pgsql/data/${db}.dmp;
dropdb ${db};
psql -c 'alter database ${db}_restore rename to $db';
rm /var/lib/pgsql/data/${db}.dmp;
EOT
)
    oc rsh -t $pod bash -c "$cmd" < "${backupdir}/${db}"
}

# ===================================================================================================
# OpenShift funcs

scale_deployments() {
  local replicas=$1
  shift
  local dcs="$@"
  for dc in $dcs; do
    oc scale dc $dc --replicas=$replicas
  done
  wait_for_deployments $replicas $dcs
}

wait_for_deployments() {
  local replicas_desired=$1
  shift
  local dcs="$@"

  oc get pods -w &
  watch_pid=$!
  for dc in $dcs; do
      echo "Waiting for $dc to be scaled to ${replicas_desired}"
      local replicas=$(get_replicas $dc)
      while [ "$replicas" -ne $replicas_desired ]; do
          echo "Sleeping 10s ..."
          sleep 10
          replicas=$(get_replicas $dc)
      done
  done
  kill $watch_pid
}

get_replicas() {
  local dc=${1}
  oc get dc $dc -o jsonpath="{.status.availableReplicas}"
}

backup_resource() {
    local backupdir=$1
    local kind=$2

    mkdir -p ${backupdir}/${kind}
    for res in $(oc get ${kind} -l app=syndesis -o name | sed -e "s/^${kind}s\///"); do
        echo "      - $res"
        oc get ${kind} $res -o yaml > "${backupdir}/${kind}/${res}"
    done
}

restore_resource() {
    local backupdir=$1
    local kind=$2

    if [ -d "${backupdir}/${kind}" ]; then
      for res in $(ls $backupdir/$kind/*); do
        echo "      - $res"
        oc create ${kind} $res -o yaml > "${backupdir}/${kind}/${res}"
      done
    fi
}

syndesis_deployments() {
  oc get dc -l app=syndesis -o name | sed -e "s/^deploymentconfigs\///"
}

pod() {
  local dc=${1}
  local ret=$(oc get pod -o name | grep "$dc" | sed -e "s/^pods\///")
  local nr_pods=$(echo $ret | wc -l | awk '$1=$1')
  if [ $nr_pods != "1" ]; then
      echo "ERROR: More than 1 pod found for $dc ($nr_pods found)"
  fi
  echo $ret
}


# =================================================================================

if [ $(hasflag --verbose) ]; then
    export PS4='+(${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'
    set -x
fi

run "$@"
