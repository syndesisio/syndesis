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

# All modules, in the right build order
ALL_MODULES="connectors verifier runtime rest s2i ui"
MODULES=(
  "ui"
  "connectors"
  "runtime:connectors"
  "verifier:connectors"
  "rest:connectors runtime"
  "s2i:rest runtime connectors"
)

# Display a help message.
display_help() {
    cat - <<EOT
Build Syndesis

Usage: build.sh [... options ...]

and the following options:

-b  --backend                 Build only backend modules (rest, verifier, runtime, connectors)
    --images                  Build only modules with Docker images (ui, rest, verifier, s2i)
-m  --module <m1>,<m2>, ..    Build modules
                              Modules: ui, rest, connectors, s2i, verifier, runtime
-d  --dependencies            Build also all project the specified module depends on
    --init                    Install top-level parent pom, too. Only needed when used with -m

    --skip-tests              Skip unit and system test execution
    --skip-checks             Disable all checks
-f  --flash                   Skip checks and tests execution (fastest mode)

-i  --image-mode  <mode>      <mode> can be
                              - "none"      : No images are build (default)
                              - "openshift" : Build for OpenShift image streams
                              - "docker"    : Build against a plain Docker daemon
                              - "auto"      : Automatically detect whether to use "s2i" or "docker"
-n  --namespace <ns>          Specifies the namespace to create images in when using '--images s2i'

-c  --clean                   Run clean builds (mvn clean)
-b  --batch-mode              Run mvn in batch mode
-r  --rebase                  Fetch origin/master and try a rebase

-h  --help                    Display this help message

With "--system-test" the system tests are triggered which know these additional options:

    --test-namespace <ns>     The test namespace to use
    --test-token <token>      The token for the test namespace
    --pool-namespace <ns>     Specify the pool namespace to use for testing (mutually exclusive with --test-namespace)
    --create-lock <prefix>    Create project pool locks for system-tests for the projects with the given prefix

With "--minishift" Minishift can be initialized and installed with Syndesis

    --reset                   Reset the minishift installation with 'minishift delete && minishift start'.
    --full-reset              Full reset by 'minishift stop && rm -rf ~/.minishift && minishift start'
    --memory                  How much memory to use when doing a reset. Default: 4GB
    --cpus                    How many CPUs to use when doing a reset. Default: 2
    --disk-size               How many disk space to use when doing a reset. Default: 20GB
    --install                 Install templates into a running Minishift
    --watch                   Watch startup of pods
-i  --image-mode <mode>       Which templates to install: "docker" for plain images, "openshift" for image streams
                              (default: "openshift")

With "--dev" common development tasks are simplified

    --debug <name>           Setup a port forwarding to <name> pod (example: rest)

Examples:

* Build only backend modules, fast               build.sh --backend --flash
* Build only UI                                  build.sh --module ui
* Build only images with OpenShift S2I, fast     build.sh --images --image-mode s2i --flash
* Build only the rest and verifier image         build.sh --module rest,verifier --image-mode s2i
* Build for system test                          build.sh --system-test
* Start Minishift afresh                         build.sh --minishift --full-reset --install --watch
* Setup debug port forward for rest pod          build.sh --dev --debug rest

EOT
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
    for var in $ARGS; do
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
# Git update functions

git_rebase_upstream() {
  echo "git fetch upstream master"
  git fetch upstream master
  echo -n "git rebase upstream/master"
  if ! git rebase upstream/master; then
    echo " (failed)"
    echo "git stash"
    git stash
    echo "git rebase upstream/master"
    git rebase upstream/master
    echo "git stash pop"
    git stash pop
  else
    echo
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
            oc annotate secret $lock syndesis.io/allocated-by=$build_name --resource-version=$version --overwrite -n $pool_namespace > /dev/null
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
    local namespace=${1:-}
    local pool_namespace=${2:-}
    local initial_namespace=${3:-}
    local initial_token=${4:-}
    echo "Cleaning up namespace: $namespace using pool namespace: $pool_namespace and switching back to namespace: $initial_namespace."

    #1. We need to login to the original project first, before releasing the lock.
    if [ -n "${initial_token:-}" ]; then
        oc login --token=${initial_token} --server=$(current_server) || true
        oc project ${pool_namespace}
    else
        echo "Warning: No initial token found!"
    fi

    if [ -n "${namespace:-}" ]; then
        echo "Releasing project: ${namespace}"
        $(release_project_lock ${namespace} ${pool_namespace}) || echo "Failed to release project: ${namespace}"
    else
        echo "Warning: No project was passed to release! Project will not get released! "
    fi

    if [ -n "${initial_namespace}" ]; then
        oc project ${initial_namespace} || true
    fi
}

# ======================================================
# Build functions

extract_modules() {
    local modules=""

    if [ "$(hasflag --backend -b)" ]; then
        modules="$modules connectors runtime rest verifier"
    fi

    if [ "$(hasflag --images)" ]; then
        modules="$modules ui rest verifier s2i"
    fi

    local arg_modules=$(readopt --module -m);
    if [ -n "${arg_modules}" ]; then
        modules="$modules ${arg_modules//,/ }"
    fi

    if [ "$(hasflag --dependencies -d)" ]; then
        local extra_modules=""
        for module in $modules; do
            for m in "${MODULES[@]}"; do
              local k=${m%%:*}
              if [ "$module" == $k ]; then
                  local v=${m#*:}
                  extra_modules="${extra_modules} $v"
              fi
            done
        done
        modules="$modules $extra_modules"
    fi
    if [ -z "$modules" ]; then
      return
    fi
    # Unique modules
    local unique_modules=$(echo $modules | xargs -n 1 | sort -u | xargs | awk '$1=$1')
    echo $(order_modules "$unique_modules")
}

order_modules() {
    # Fix order
    local modules="$1"
    # All modules in the proper order
    local ret=$ALL_MODULES
    for cm in "${MODULES[@]}"; do
      local check_module=${cm%%:*}
      # Check if $check_module is in the module list
      if [ -n "${modules##*${check_module}*}" ]; then
        # No, so remove it from the return value
        ret=${ret//$check_module/}
      fi
    done

    # Normalize return value
    echo $ret | awk '$1=$1'
}

join_comma() {
    local IFS=","
    echo "$*"
}

get_maven_args() {
    local namespace=${1:-}
    local args=""

    if [ -n "$(hasflag --flash -f)" ]; then
        args="$args -Pflash"
    fi

    if [ -n "$(hasflag --skip-tests)" ]; then
        args="$args -DskipTests"
    fi

    if [ -n "$(hasflag --skip-checks)" ]; then
        args="$args -Pskip-checks"
    fi

    if [ -n "$(hasflag --batch-mode -b)" ]; then
        args="$args --batch-mode"
    fi

    local image_mode="$(readopt --image-mode -i)"
    if [ "${image_mode}" != "none" ]; then
        if [ -n "$(hasflag --images)" ] || [ -n "${image_mode}" ]; then
            #Build images
            args="$args -Pimage"
            if [ -n "${image_mode}" ]; then
                if [ "${image_mode}" == "openshift" ] || [ "${image_mode}" == "s2i" ]; then
                    args="$args -Dfabric8.mode=openshift"
                elif [ "${image_mode}" == "docker" ]; then
                    args="$args -Dfabric8.mode=kubernetes"
                elif [ "${image_mode}" != "auto" ]; then
                    echo "ERROR: Invalid --image-mode ${image_mode}. Only 'none', 'openshift', 'docker' or 'auto' supported".
                    exit 1
                fi
            fi
        fi
    fi

    local ns="$namespace"
    if [ -z "$ns" ]; then
        ns="$(readopt --namespace -n)"
    fi
    if [ -n "${ns}" ]; then
        args="$args -Dfabric8.namespace=${ns}"
    fi

    if [ -n "$(hasflag --clean -c)" ]; then
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

run_mvnw() {
    local args=$1
    local maven_modules=$(extract_modules)
    check_error $maven_modules
    cd $(basedir)
    if [ -z "${maven_modules}" ]; then
        echo "=============================================================================="
        echo "./mvnw $args"
        echo "=============================================================================="
        ./mvnw $args
    else
      echo "Modules: $maven_modules"
      if [ $(hasflag --init) ]; then
        echo "=============================================================================="
        echo "./mvnw -N install"
        ./mvnw -N install
      fi
      for module in $maven_modules; do
        echo "=============================================================================="
        echo "./mvnw $args -f $module"
        echo "=============================================================================="
        ./mvnw -f $module $args
      done
    fi
}

run_build() {
    run_mvnw "$(get_maven_args)"
}

run_test() {
    local initial_token=$(current_token)
    local initial_namespace=$(oc project -q)
    local pool_namespace=$(readopt --pool-namespace)

    local test_namespace=$(readopt --test-namespace)
    local test_token=$(readopt --test-token)

    local build_id=${JOB_NAME:-}${BUILD_NUMBER:-}
    if [ -z "$build_id" ] && [ -n "${CIRCLE_JOB:-}" ]; then
       build_id="${CIRCLE_JOB}${CIRCLE_BUILD_NUM:-}"
    fi

    if [ -z "$build_id" ]; then
        build_id="cli"
    fi

    if [ -z "$test_namespace" ] || [ -z "$test_token" ]; then
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
        for r in {1..10}; do
            if [ -z "$namespace" ]; then
                echo "Couldn't obtain lock. Retrying in 1 minute."
                sleep 1m
                if [ -n "${lock:-}" ]; then
                    lock=$(obtain_project_lock $build_id $pool_namespace)
                    namespace=$(project_lock_name $lock $pool_namespace)
                    token=$(project_lock_token $lock $pool_namespace)
                fi
            fi
        done

        if [ -z "$namespace" ]; then
            echo "Failed to allocate lock! Exiting!"
            exit -1
        fi

        echo "Allocated project: $namespace for: $build_id"
        oc login --token=$token --server=$(current_server)

        if [ -z "${namespace}" ]; then
            echo "Could not determine test namespace"
            exit 1
        fi

        trap "teardown_project_pool $namespace $pool_namespace $initial_namespace $initial_token" EXIT
    else
        echo "Using test namespace: $test_namespace"
        namespace=$test_namespace
        oc login --token=$test_token --server=$(current_server)
        trap "oc login --token=$initial_token --server=$(current_server) && oc project $initial_namespace" EXIT
    fi

    oc project $namespace
    export NAMESPACE_USE_EXISTING=$namespace
    export KUBERNETES_NAMESPACE=$namespace
    export OPENSHIFT_TEMPLATE_FROM_WORKSPACE=true
    export WORKSPACE=deploy

    local maven_args=$(get_maven_args $namespace)
    check_error $maven_args
    maven_args="-Psystem-tests -Pimages -Dfabric8.mode=openshift $maven_args"
    run_mvnw "$maven_args"
}

run_minishift() {
    if [ $(hasflag --full-reset) ] || [ $(hasflag --reset) ]; then
        # Only warning if minishift is not installed
        minishift delete --clear-cache --force
        if [ $(hasflag --full-reset) ] && [ -d ~/.minishift ]; then
            rm -rf ~/.minishift
        fi
        local memory=$(readopt --memory)
        local cpus=$(readopt --cpus)
        local disksize=$(readopt --disk-size)
        minishift start --memory ${memory:-4912} --cpus ${cpus:-2} --disk-size ${disksize:-20GB}
    fi

    local image_mode=$(readopt --image-mode -i)
    local template="syndesis-restricted"
    if [ "$image_mode" == "openshift" ]; then
        template="syndesis-restricted"
    elif [ "$image_mode" == "docker" ]; then
        template="syndesis-dev-restricted"
    fi
    if [ $(hasflag --install) ]; then
        basedir=$(basedir)
        check_error "$basedir"
        oc create -f ${basedir}/deploy/support/serviceaccount-as-oauthclient-restricted.yml
        oc create -f ${basedir}/deploy/${template}.yml
        oc new-app ${template} \
          -p ROUTE_HOSTNAME=syndesis.$(minishift ip).nip.io \
          -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
          -p OPENSHIFT_PROJECT=$(oc project -q) \
          -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)
    fi
    if [ $(hasflag --watch) ]; then
        watch oc get pods
    fi
}

dev_tasks() {
    if [ $(hasflag --debug) ]; then
        local name=$(readopt --debug)
        if [ -z "${name}" ]; then
            name="rest"
        fi

        local pod=$(oc get -o name pod -l component=syndesis-${name})
        oc port-forward ${pod//*\//} 5005:5005
    fi
}

# ============================================================================
# Main loop

if [ -n "$(hasflag --help -h)" ]; then
    display_help
    exit 0
fi

if [ -n "$(hasflag --rebase -r)" ]; then
    git_rebase_upstream
fi

# RUn minishift tasks
if [ -n "$(hasflag --minishift)" ]; then
    run_minishift
    exit 0
fi

# Run system tests
if [ -n "$(hasflag --system-test)" ]; then
    lock_prefix=$(readopt --create-lock)
    if [ -n "${lock_prefix}" ]; then
        create_lock $lock_prefix
        exit 0
    fi
    run_test
    exit 0
fi

# Developer helper tasks
if [ -n "$(hasflag --dev)" ]; then
    dev_tasks
    exit 0
fi

# Check for the mode to use
mode=$(readopt --mode)
if [ -z "${mode}" ]; then
    mode="build"
fi

case $mode in
    "build")
        run_build
        exit 0
        ;;
    "system-test")
        lock_prefix=$(readopt --create-lock)
        if [ -n "${lock_prefix}" ]; then
            create_lock $lock_prefix
            exit 0
        fi

        run_test
        exit 0
        ;;
    "minishift")
        run_minishift
        exit 0
        ;;
    "dev")
        dev_tasks
        exit 0
        ;;
    **)
        echo "Invalid mode '$mode'. Known modes: build, system-test"
        exit 1
esac
