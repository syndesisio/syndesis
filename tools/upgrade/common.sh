#!/bin/bash

# ========================
# Common utility functions
# ========================


# ===================================================================================================
# Openshift funcs

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
      local replicas=$(get_replicas $dc $replicas_desired)
      while [ "$replicas" -ne $replicas_desired ]; do
          echo "Sleeping 10s ..."
          sleep 10
          replicas=$(get_replicas $dc $replicas_desired)
      done
  done
  kill $watch_pid
}

backup_resource() {
    local backupdir=$1
    local kind=$2

    mkdir -p ${backupdir}/${kind}
    for res in $(oc get ${kind} -l syndesis.io/app=syndesis,syndesis.io/type=infrastructure -o custom-columns=:.metadata.name | tail -n +2); do
        echo "        * $res"
        oc get ${kind} $res -o json > "${backupdir}/${kind}/${res}.json"
    done
}

syndesis_deployments() {
  oc get dc -l syndesis.io/app=syndesis,syndesis.io/type=infrastructure -o custom-columns=:.metadata.name | tail -n +2
}

pod() {
  local dc=${1}
  local not=${2:-}
  local ret=$(oc get pod -o custom-columns=:.metadata.name | tail -n +2 | grep "$dc" | grep -v "\-deploy")
  if [ -n "$not" ]; then
      ret=$(echo $ret | grep -v $not)
  fi
  local nr_pods=$(echo $ret | wc -l | awk '$1=$1')
  if [ $nr_pods != "1" ]; then
      echo "ERROR: More than 1 pod found for $dc ($nr_pods found)"
  fi
  echo $ret
}

get_replicas() {
  local dc=${1}
  local replicas_desired=${2:-}
  if [ -n "$replicas_desired" ] && [ $replicas_desired -eq 0 ]; then
      # For a downscale to zero, we should really wait until all pods are gone
      echo $(get_running_or_terminating_pods $dc)
  else
      # For an upscale, wait until the number of replicas are really available
      # (i.e. being ready to serve)
      oc get dc $dc -o jsonpath="{.status.availableReplicas}"
  fi
}

get_running_or_terminating_pods() {
  local dc=${1}
  local pod_nr=$(
     oc get pod                                                     \
        -l deploymentconfig=$dc                                     \
        -o jsonpath='{range .items[*]}{.status.phase}{"\n"}{end}' | \
     grep "Running\|Terminating"                                  | \
     wc -l                                                        | \
     awk '$1=$1'
  )
  if [ -z "$pod_nr" ]; then
    echo 0
  else
    echo $pod_nr
  fi
}

# Read from global config
read_global_config() {
    local key=$1
    oc get secret syndesis-global-config -o jsonpath={.data.${key}} | base64 --decode
}

# Read a single parameter
extract_param() {
    local key=${1}
    read_global_config params | grep "${key}" | sed -e "s/${key}=//"
}

# ===================================================================================================
# Postgres funcs

pg_backup() {
    local backupdir=${1}
    local db=${2}

    local pod=$(pod "syndesis-db" "syndesis-db-metrics")
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

    local pod=$(pod "syndesis-db" "syndesis-db-metrics")
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

# ==================================================================================


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
