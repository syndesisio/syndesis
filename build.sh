#!/bin/bash

# Exit if any error occurs
set -e
set -x

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Save global script args
ARGS="$@"

CURRENT_TOKEN=""
CURRENT_SERVER=""

# Display a help message.
function displayHelp() {
    echo "This script helps you build the syndesis monorepo."
    echo "The available options are:"
    echo " --skip-tests            Skips the test execution."
    echo " --skip-image-builds     Skips image builds."
    echo " --with-image-streams    Builds everything using image streams."
    echo " --namespace N           Specifies the namespace to use."
    echo " --pool-namespace N      Specifies the pool namespace to use."
    echo " --resume-from           Resume build from module."
    echo " --clean                 Cleans up the projects."
    echo " --batch-mode            Runs mvn in batch mode."
    echo " --help                  Displays this help message."
}

#
# Checks if a flag is present in the arguments.
function hasflag() {
    filter=$1
    for var in "${@:2}"; do
        if [ "$var" = "$filter" ]; then
            echo 'true'
            break;
        fi
    done
}

#
# Read the value of an option.
function readopt() {
        filter=$1
        next=false
        for var in "${@:2}"; do
                if $next; then
                        echo $var
                        break;
                fi
                if [ "$var" = "$filter" ]; then
                        next=true
                fi
        done
}

#
# Gets the current user token.
function current_token() {
    local TMP_FILE=$(mktemp /tmp/syndesis-build-script.XXXXXX)
    oc whoami --loglevel=8 > /dev/null 2> $TMP_FILE
    cat $TMP_FILE | grep Authorization | awk -F "Bearer " '{print $2}'
    rm $TMP_FILE 2> /dev/null
}

# ======================================================
# Lock functions (secret lock strategy)

# Displays the data of the lock. A project lock is a secret that contains the following:
# 1. annotations:
#    i)  syndesis.io/lock-for-project: The project that this lock corresponds to.
#    ii) syndesis.io/allocated-by:     The owner of the lock.
# 2. data:
#    i)  connection information for the project (token)
function project_lock_data() {
    local SECRET_NAME=$1
    local POOL_NAMESPACE=$2
    oc get secret $SECRET_NAME -n $POOL_NAMESPACE -o go-template='{{index .metadata.annotations "syndesis.io/lock-for-project"}} {{.metadata.resourceVersion}} {{index .metadata.annotations "syndesis.io/allocated-by"}}{{"\n"}}'
}

#
# Obtains a project lock. (See above).
function obtain_project_lock() {
    local BUILD_NAME=$1
    local POOL_NAMESPACE=$2
    for lock in `oc get secret -n $POOL_NAMESPACE | grep project-lock- | awk -F " " '{print $1}'`; do
        echo "Trying to obtain lock data from secret $lock" > /tmp/log
        status=$(project_lock_data $lock $POOL_NAMESPACE)
        project=`echo $status | awk -F " " '{print $1}'`
        version=`echo $status | awk -F " " '{print $2}'`
        allocator=`echo $status | awk -F " " '{print $3}'`

        if [ -z "$allocator" ] || [ "$BUILD_NAME" == "$allocator" ]; then
            oc annotate secret $lock syndesis.io/allocated-by=$BUILD_NAME --resource-version=$version --overwrite -n $POOL_NAMESPACE > /dev/null
            newstatus=$(project_lock_data $lock $POOL_NAMESPACE)
            newallocator=`echo $newstatus | awk -F " " '{print $3}'`
            if [ "$newallocator" == "$BUILD_NAME" ]; then
                echo $lock
                return
            fi
        fi
    done
}

function project_lock_token() {
    local SECRET_NAME=$1
    local POOL_NAMESPACE=$2
    oc get secret $SECRET_NAME -n $POOL_NAMESPACE -o yaml | grep token: | awk -F ": " '{print $2}' | base64 -d
}

function project_lock_name() {
    local SECRET_NAME=$1
    local POOL_NAMESPACE=$2
    oc get secret $SECRET_NAME -n $POOL_NAMESPACE -o go-template='{{index .metadata.annotations "syndesis.io/lock-for-project"}}{{"\n"}}'
}

function release_project_lock() {
    local POOL_NAMESPACE=$2
    oc annotate secret $1 syndesis.io/allocated-by="" --overwrite -n $POOL_NAMESPACE > /dev/null
}

function init_project_pool() {
    BUILD_ID=$JOB_NAME$BUILD_NUMBER
    if [ -z "$BUILD_ID" ]; then
        BUILD_ID="cli"
    fi

    echo "Trying to allocate project for: $BUILD_ID"

    if [ $(which oc) ] ; then
        echo "oc binary found. Proceeding with pool initialization."
    else
        echo "Warning: oc binary not found in path. Disabling allocation of test project!"
        return
    fi

    CURRENT_TOKEN=$(current_token)
    CURRENT_SERVER=`oc whoami --show-server`
    INITIAL_NAMESPACE=$(oc project -q)
    POOL_NAMESPACE=$(readopt --pool-namespace $ARGS 2> /dev/null)
    if [ -z "$POOL_NAMESPACE" ]; then
        POOL_NAMESPACE="syndesis-ci"
    fi

    echo "Using pool namespace: $POOL_NAMESPACE"

    LOCK=$(obtain_project_lock $BUILD_ID $POOL_NAMESPACE)
    if [ -n "$LOCK" ]; then
        echo "Obtained lock: $LOCK"
    fi

    NAMESPACE=$(project_lock_name $LOCK $POOL_NAMESPACE)
    TOKEN=$(project_lock_token $LOCK $POOL_NAMESPACE)
    while [ -z "$NAMESPACE" ]; do
        echo "Couldn't obtain lock. Retrying in 1 minute."
        sleep 1m

        if [ -n "$LOCK" ]; then
            LOCK=$(obtain_project_lock $BUILD_ID $POOL_NAMESPACE)
            NAMESPACE=$(project_lock_name $LOCK $POOL_NAMESPACE)
            TOKEN=$(project_lock_token $LOCK $POOL_NAMESPACE)
        fi
    done

    echo "Allocated project: $NAMESPACE for: $BUILD_ID"
    oc login --token=$TOKEN --server=$CURRENT_SERVER
    trap 'teardown_project_pool $NAMESPACE $POOL_NAMESPACE $INITIAL_NAMESPACE' EXIT
    MAVEN_PARAMS="$MAVEN_PARAMS -Dfabric8.namespace=$NAMESPACE"
    OC_OPTS=" -n $NAMESPACE"
}

function teardown_project_pool() {
    local NAMESPACE=$1
    local POOL_NAMESPACE=$2
    local INITIAL_NAMESPACE=$3

    #1. We need to login to the original project first, before releasing the lock.
    if [ -n "$CURRENT_TOKEN" ]; then
        oc login --token=$CURRENT_TOKEN --server=$CURRENT_SERVER || true
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

function modules_to_build() {
  modules="parent tests"
  resume_from=$(readopt --resume-from $ARGS 2> /dev/null)
  if [ "x${resume_from}" != x ]; then
    modules=$(echo $modules | sed -e "s/^.*$resume_from/$resume_from/")
  fi
  echo $modules
}

function init_options() {
  SKIP_TESTS=$(hasflag --skip-tests $ARGS 2> /dev/null)
  SKIP_IMAGE_BUILDS=$(hasflag --skip-image-builds $ARGS 2> /dev/null)
  CLEAN=$(hasflag --clean $ARGS 2> /dev/null)
  WITH_IMAGE_STREAMS=$(hasflag --with-image-streams $ARGS 2> /dev/null)
  NAMESPACE=$(readopt --namespace $ARGS 2> /dev/null)
  HELP=$(hasflag --help $ARGS 2> /dev/null)

  # Internal variable default values
  OC_OPTS=""
  MAVEN_PARAMS=""
  MAVEN_CLEAN_GOAL="clean"
  MAVEN_IMAGE_BUILD_GOAL="fabric8:build"
  MAVEN_CMD="${MAVEN_CMD:-${BASEDIR}/mvnw}"
  LOCK=""

  # If  we are running in cicleci lets configure thigs to avoid running out of memory:
  if [ "$CIRCLECI" == "true" ] ; then
    MAVEN_PARAMS="$MAVEN_PARAMS -Dbasepom.test.fork-count=2"
  fi

  # Apply options
  if [ -n "$(hasflag --batch-mode $ARGS 2> /dev/null)" ]; then
    MAVEN_PARAMS="$MAVEN_PARAMS --batch-mode"
  fi

  if [ -n "$SKIP_TESTS" ]; then
      echo "Skipping tests ..."
      MAVEN_PARAMS="$MAVEN_PARAMS -DskipTests"
  fi

  if [ -n "$SKIP_IMAGE_BUILDS" ]; then
      echo "Skipping image builds ..."
      MAVEN_IMAGE_BUILD_GOAL=""
  fi

  if [ -z "$POOL_NAMESPACE" ];then
      POOL_NAMESPACE="syndesis-ci"
  fi

  if [ -n "$NAMESPACE" ]; then
      echo "Namespace: $NAMESPACE"
      MAVEN_PARAMS="$MAVEN_PARAMS -Dfabric8.namespace=$NAMESPACE"
      OC_OPTS=" -n $NAMESPACE"
  else
      init_project_pool
  fi

  if [ -z "$CLEAN" ];then
      MAVEN_CLEAN_GOAL=""
  fi

  if [ -n "$WITH_IMAGE_STREAMS" ]; then
    echo "With image streams ..."
    MAVEN_PARAMS="$MAVEN_PARAMS -Dfabric8.mode=openshift"
  else
    MAVEN_PARAMS="$MAVEN_PARAMS -Dfabric8.mode=kubernetes"
  fi

  if [ -z "$BUILD_ID" ]; then
      BUILD_ID="cli"
  fi
}

function parent() {
  "${MAVEN_CMD}" $MAVEN_CLEAN_GOAL install $MAVEN_PARAMS
}

function rest() {
  pushd rest
  "${MAVEN_CMD}" $MAVEN_CLEAN_GOAL install $MAVEN_IMAGE_BUILD_GOAL $MAVEN_PARAMS
  popd
}

function s2i() {
  pushd s2i
  "${MAVEN_CMD}" $MAVEN_CLEAN_GOAL install $MAVEN_PARAMS
  popd
}

function ui() {
  pushd ui
  "${MAVEN_CMD}" $MAVEN_CLEAN_GOAL install $MAVEN_PARAMS
  # yarn
  # yarn ng build -- --aot --prod --progress=false
  # if [ -z "$SKIP_IMAGE_BUILDS" ]; then
  #     if [ -n "$WITH_IMAGE_STREAMS" ]; then
  #         BC_DETAILS=`oc get bc | grep syndesis-ui || echo ""`
  #         if [ -z "$BC_DETAILS" ]; then
  #             cat docker/Dockerfile | oc new-build --dockerfile=- --to=syndesis/syndesis-ui:latest --strategy=docker $OC_OPTS
  #         fi
  #         tar -cvf archive.tar dist docker
  #         oc start-build -F --from-archive=archive.tar syndesis-ui $OC_OPTS
  #         rm archive.tar
  #     else
  #         docker build -t syndesis/syndesis-ui:latest -f docker/Dockerfile . | cat -
  #     fi
  # fi
  popd
}

function tests() {
    if [ $(which oc) ] ; then
        echo "oc binary found. Proceeding with system tests."
    else
        echo "Warning: oc binary not found in path. Disabling system tests!"
        return
    fi

    pushd tests
    export NAMESPACE_USE_EXISTING=$NAMESPACE
    export KUBERNETES_NAMESPACE=$NAMESPACE
    export OPENSHIFT_TEMPLATE_FROM_WORKSPACE=true
    export WORKSPACE=../deploy
    mvn clean install
    popd
}

# ============================================================================
# Main loop

init_options

if [ -n "$HELP" ]; then
   displayHelp
   exit 0
fi

for module in $(modules_to_build)
do
  echo "=========================================================="
  echo "Building ${module} ...."
  echo "=========================================================="
  eval "${module}"
done
