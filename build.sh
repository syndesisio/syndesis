#!/bin/bash

# ===================================================================================================
# Syndesis Build Script
#
# See `build.sh --help` for usage information
# ==================================================================================================

# Exit if any error occurs
# Fail on a single failed command in a pipeline (if supported)
set -o pipefail

# Fail on error and undefined vars (please don't use global vars, but evaluation of functions for return values)
set -eu

# Save global script args
ARGS="$@"

# Display a help message.
display_help() {
  cat - <<EOT
Build Syndesis

Usage: build.sh [--mode <mode>] [... options ...]

with the following modes:

build       -- Developer builds (without system test). This is the default if no mode is given. Images are *not*
               build by default, use --images or --image-mode to switch this on.
system-test -- Run the build and the system test. Needs an valid OpenShift login.

and the following options:

  --backend               Build only backend modules (rest, verifier, runtime, connectors)
  --images                Build only modules with Docker images (ui, rest, verifier, s2i).
  --module <m1>,<m2>, ..  Build multiple modules (and their dependencies and dependent modules)
                          Modules: ui, rest, connectors, s2i, verifier, runtime
  --dependencies          Build also all project the specified module depends on

  --skip-tests            Skip unit and system test execution
  --skip-checks           Disable all checks
  --flash                 Skip checks and tests execution (fastest mode)

  --image-mode  <mode>    <mode> can be:
                          - "none"   : No images are build (default)
                          - "s2i"    : Build for OpenShift image streams
                          - "docker" : Build against a plain Docker daemon
                          - "auto"   : Automatically detect whether to use "s2i" or "docker"
  --namespace <ns>        Specifies the namespace to create images in when using ''--images s2i'

  --clean                 Run clean builds (mvn clean)
  --batch-mode            Run mvn in batch mode

  --pool-namespace <ns>   Specify the pool namespace to use for testing
  --create-lock <prefix>  Create project pool locks for system-tests for the projects with the given prefix
  --help                  Display this help message

Examples:

* Build only backend modules, fast               build.sh --backend --flash
* Build only UI                                  build.sh --module ui
* Build only images with OpenShift S2I, fast     build.sh --images --image-mode s2i --flash
* Build only the rest and verifier image         build.sh --module rest,verifier --image-mode s2i
* Build for system test                          build.sh --mode system-test

EOT
}

# Dir where this script is located
basedir() {
    # Default is current directory
    local dir=$(dirname "$0")
    local full_dir=$(cd "${dir}" && pwd)
    echo ${full_dir}
}

# Checks if a flag is present in the arguments.
hasflag() {
  filter=$1
  for var in $ARGS; do
    if [ "$var" = "$filter" ]; then
      echo 'true'
      break;
    fi
  done
}

# Read the value of an option.
readopt() {
  filters="$@"
  next=false
  for var in $ARGS; do
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

# ======================================================
# Testing functions

base64_decode_option() {
  set +e
  for opt in -D -d; do
    echo "Y2hpbGk=" | base64 $opt >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo $opt
      set -e
      return
    fi
  done
  set -e
  echo "ERROR: Neither base64 -d nor base64 -D works"
}

find_secret() {
	local namespace=$1
	local service_account=$2
	oc get sa $service_account -n $namespace -o yaml | grep ${service_account}-token- | awk -F ": " '{print $2}'
}

read_token() {
	local secret=$1
	local namespace=$2
  base64_opt=$(base64_decode_option)
  check_error $base64_opt
	oc get secret $secret -n $namespace -o yaml | grep token: | awk -F ": " '{print $2}' | base64 $base64_opt
}

read_token_of_sa() {
	local namespace=$1
	local service_account=$2
	local secret=$(find_secret $namespace $service_account)
	local token=$(read_token $secret $namespace)
	echo $token
}

# Create a lock for all projects with the given prefix
create_lock() {
  local prefix=$1
  local service_account="default"
  local pool_namespace=$(readopt --pool-namespace)

  if [ -z "$pool_namespace" ]; then
      pool_namespace="syndesis-ci"
  fi

  for p in $(oc get projects | grep $prefix | awk -F " " '{print $1}'); do
	  echo "Creating a secret lock for project $p"
	  local secret=$(find_secret $p "default")
	  echo "Found secret: $secret"
  	local token=$(read_token_of_sa $p $service_account)
	  echo "Found token: $token"
  	oc delete  secret project-lock-$p -n $pool_namespace || true
	  oc create secret generic project-lock-$p --from-literal=token=$token -n $pool_namespace
	  oc annotate secret project-lock-$p syndesis.io/lock-for-project=$p -n $pool_namespace
	  oc annotate secret project-lock-$p syndesis.io/allocated-by="" -n $pool_namespace

	  oc adm policy add-role-to-user edit system:serviceaccount:$p:$service_account -n $p
	  oc adm policy add-role-to-user system:image-puller system:serviceaccount:$p:$service_account -n $p
	  oc adm policy add-role-to-user system:image-builder system:serviceaccount:$p:$service_account -n $p
  done
}

# Gets the current OpenShift user token.
current_token() {
  echo $(oc whoami -t)
}

# Get the current OpenShift server
current_server() {
  echo $(oc whoami --show-server)
}

# Lock functions (secret lock strategy)

# Displays the data of the lock. A project lock is a secret that contains the following:
# 1. annotations:
#    i)  syndesis.io/lock-for-project: The project that this lock corresponds to.
#    ii) syndesis.io/allocated-by:     The owner of the lock.
# 2. data:
#    i)  connection information for the project (token)
project_lock_data() {
  local secret_name=$1
  local pool_namespace=$2
  oc get secret $secret_name -n $pool_namespace -o go-template='{{index .metadata.annotations "syndesis.io/lock-for-project"}}~{{.metadata.resourceVersion}}~{{index .metadata.annotations "syndesis.io/allocated-by"}}{{"\n"}}'
}

#
# Obtains a project lock. (See above).
obtain_project_lock() {
  local build_name=$1
  local pool_namespace=$2
  for lock in $(oc get secret -n $pool_namespace | grep project-lock- | awk -F " " '{print $1}'); do
    echo "Trying to obtain lock data from secret $lock" > /tmp/log
    local status=$(project_lock_data $lock $pool_namespace)
    local project=`echo $status | awk -F "~" '{print $1}'`
    local version=`echo $status | awk -F "~" '{print $2}'`
    local allocator=`echo $status | awk -F "~" '{print $3}'`

    if [ -z "$allocator" ] || [ "$build_name" == "$allocator" ]; then
      oc annotate secret $lock syndesis.io/allocated-by=$build_name syndesis.io/lock-for-project=$pool_namespace --resource-version=$version --overwrite -n $pool_namespace > /dev/null
      local newstatus=$(project_lock_data $lock $pool_namespace)
      local newallocator=`echo $newstatus | awk -F "~" '{print $3}'`
      if [ "$newallocator" == "$build_name" ]; then
        echo $lock
        return
      fi
    fi
  done
}

project_lock_token() {
  local secret_name=$1
  local pool_namespace=$2
  local base64_opt=$(base64_decode_option)
  check_error $base64_opt
  oc get secret $secret_name -n $pool_namespace -o yaml | grep token: | awk -F ": " '{print $2}' | base64 $base64_opt
}

project_lock_name() {
  local secret_name=$1
  local pool_namespace=$2
  oc get secret $secret_name -n $pool_namespace -o go-template='{{index .metadata.annotations "syndesis.io/lock-for-project"}}{{"\n"}}'
}

release_project_lock() {
  local lock="project-lock-$1"
  local pool_namespace=$2
  oc annotate secret $lock syndesis.io/allocated-by="" --overwrite -n $pool_namespace > /dev/null
}


teardown_project_pool() {
  local NAMESPACE=$1
  local POOL_NAMESPACE=$2
  local INITIAL_NAMESPACE=$3
  local INITIAL_TOKEN=$4

  echo "oc login --token=$INITIAL_TOKEN --server=$(current_server)"
  #1. We need to login to the original project first, before releasing the lock.
  if [ -n "${INITIAL_TOKEN:-}" ]; then
    oc login --token=$INITIAL_TOKEN --server=$(current_server) || true
    oc project $POOL_NAMESPACE
  fi

  if [ -n "$NAMESPACE" ]; then
    $(release_project_lock $NAMESPACE $POOL_NAMESPACE) || true
  fi

  if [ -n "$INITIAL_NAMESPACE" ]; then
    oc project $INITIAL_NAMESPACE || true
  fi
}

# ======================================================
# Build functions

extract_modules() {
  local modules=""

  if [ "$(hasflag --backend)" ]; then
    modules="$modules connectors runtime rest verifier"
  fi

  if [ "$(hasflag --images)" ]; then
    modules="$modules ui rest verifier"
  fi

  local arg_modules=$(readopt --module -m);
  if [ -n "${arg_modules}" ]; then
    modules="$modules ${arg_modules//,/ }"
  fi

  # Unique modules
  echo "$modules" | xargs -n 1 | sort -u | xargs | awk '$1=$1'
}

args_for_modules() {
  local modules="$*"
  if [ -n "${modules}" ]; then
    local args="-pl $(join_comma ${modules})"
    if [ -n "$(hasflag --dependencies)" ]; then
      args="${args} -am"
    fi
    args="${args} -amd"
    echo "$args"
  fi
}

join_comma() {
  local IFS=","
  echo "$*"
}

get_maven_args() {
  local namespace=${1:-}
  local modules=$(extract_modules)
  local args=$(args_for_modules $modules)

  if [ -n "$(hasflag --flash)" ]; then
    args="$args -Pflash"
  fi

  if [ -n "$(hasflag --skip-tests)" ]; then
    args="$args -DskipTests"
  fi

  if [ -n "$(hasflag --skip-checks)" ]; then
    args="$args -Pskip-checks"
  fi

  if [ -n "$(hasflag --batch-mode)" ]; then
    args="$args --batch-mode"
  fi

  local image_mode="$(readopt --image-mode)"
  if [ "${image_mode}" != "none" ]; then
    if [ -n "$(hasflag --images)" ] || [ -n "${image_mode}" ]; then
      #Build images
      args="$args -Pimage"
      if [ -n "${image_mode}" ]; then
        if [ "${image_mode}" == "s2i" ]; then
          args="$args -Dfabric8.mode=openshift"
        elif [ "${image_mode}" == "docker" ]; then
          args="$args -Dfabric8.mode=kubernetes"
        elif [ "${image_mode}" != "auto" ]; then
          echo "ERROR: Invalid --image-mode ${image_mode}. Only 'none', 's2i', 'docker' or 'auto' supported".
          exit 1
        fi
      fi
    fi
  fi

  local ns="$namespace"
  if [ -z "$ns" ]; then
    ns="$(readopt --namespace)"
  fi
  if [ -n "${ns}" ]; then
    args="$args -Dfabric8.namespace=${ns}"
  fi

  if [ -n "$(hasflag --clean)" ]; then
    args="$args clean"
  fi

  local goals="$(readopt --goals)"
  if [ -n "${goals}" ]; then
    args="$args ${goals//,/ }"
  else
    args="$args install"
  fi

  echo $args
}

run_build() {
  local maven_args=$(get_maven_args)
  check_error $maven_args
  echo "./mvnw $maven_args"
  exec $(basedir)/mvnw $maven_args
}

run_test() {
  local initial_token=$(current_token)
  local initial_namespace=$(oc project -q)
  local pool_namespace=$(readopt --pool-namespace)

  local build_id=${JOB_NAME:-}${BUILD_NUMBER:-}
  if [ -z "$build_id" ]; then
    build_id="cli"
  fi
  echo "Trying to allocate project for: $build_id"

  if [ -z "$pool_namespace" ]; then
    pool_namespace="syndesis-ci"
  fi

  local pool_namespace_status=$(oc get projects | grep $pool_namespace | awk -F " " '{print $2}')
  if [ "$pool_namespace_status" != "Active" ]; then
    echo "No active namespace $pool_namespace: $pool_namespace_status"
    exit 1
  fi

  echo "Using pool namespace: $pool_namespace"

  local lock=$(obtain_project_lock $build_id $pool_namespace)
  if [ -n "${lock:-}" ]; then
    echo "Obtained lock: $lock"
  fi

  local namespace=$(project_lock_name $lock $pool_namespace)
  local token=$(project_lock_token $lock $pool_namespace)
  while [ -z "$namespace" ]; do
    echo "Couldn't obtain lock. Retrying in 1 minute."
    sleep 1m

    if [ -n "${lock:-}" ]; then
      lock=$(obtain_project_lock $build_id $pool_namespace)
      namespace=$(project_lock_name $lock $pool_namespace)
      token=$(project_lock_token $lock $pool_namespace)
    fi
  done

  echo "Allocated project: $namespace for: $build_id"
  oc login --token=$token --server=$(current_server)

  if [ -z "${namespace}" ]; then
    echo "Could not determine test namespace"
    exit 1
  fi

  trap "teardown_project_pool $namespace $pool_namespace $initial_namespace $initial_token" EXIT
  export NAMESPACE_USE_EXISTING=$namespace
  export KUBERNETES_NAMESPACE=$namespace
  export OPENSHIFT_TEMPLATE_FROM_WORKSPACE=true
  export WORKSPACE=deploy

  local maven_args=$(get_maven_args $namespace)
  check_error $maven_args
  maven_args="$maven_args -Psystem-tests"
  echo "./mvnw $maven_args"
  exec $(basedir)/mvnw $maven_args
}

# ============================================================================
# Main loop

if [ -n "$(hasflag --help)" ]; then
  display_help
  exit 0
fi

mode=$(readopt --mode)
if [ -z "${mode}" ]; then
  mode="build"
fi

case $mode in
  "build")
    run_build
    ;;
  "system-test")
    lock_prefix=$(readopt --create-lock)
    if [ -n "${lock_prefix}" ]; then
      create_lock $lock_prefix
      exit 0
    fi

    run_test
    ;;
  **)
    echo "Invalid mode '$mode'. Known modes: build, system-test"
    exit 1
esac
