#!/bin/bash

# Exit if any error occurs
set -e

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Save global script args
ARGS="$@"

# Display a help message.
function displayHelp() {
    echo "This script helps you build the syndesis monorepo."
    echo "The available options are:"
    echo " --skip-tests            Skips the test execution."
    echo " --skip-image-builds     Skips image builds."
    echo " --with-image-streams    Builds everything using image streams."
    echo " --namespace N           Specifies the namespace to use."
    echo " --resume-from           Resume build from module."
    echo " --clean                 Cleans up the projects."
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

# ======================================================
# Build functions

function modules_to_build() {
  modules="connectors verifier runtime rest ui"
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
  MAVEN_OPTS=""
  MAVEN_CLEAN_GOAL="clean"
  MAVEN_IMAGE_BUILD_GOAL="fabric8:build"

  # Apply options
  if [ -n "$SKIP_TESTS" ]; then
      echo "Skipping tests ..."
      MAVEN_OPTS="$MAVEN_OPTS -DskipTests"
  fi

  if [ -n "$SKIP_IMAGE_BUILDS" ]; then
      echo "Skipping image builds ..."
      MAVEN_IMAGE_BUILD_GOAL=""
  fi

  if [ -n "$NAMESPACE" ]; then
      echo "Namespace: $NAMESPACE"
      MAVEN_OPTS="$MAVEN_OPTS -Dfabric8.namespace=$NAMESPACE"
      OC_OPTS=" -n $NAMESPACE"
  fi

  if [ -z "$CLEAN" ];then
      MAVEN_CLEAN_GOAL=""
  fi

  if [ -n "$WITH_IMAGE_STREAMS" ]; then
    echo "With image streams ..."
    MAVEN_OPTS=" -Dfabric8.mode=openshift"
  else
    MAVEN_OPTS=" -Dfabric8.mode=kubernetes"
  fi
}

function connectors() {
  pushd connectors
  # We are ALWAYS clean this project, because build fails otherwise: https://github.com/syndesisio/connectors/issues/93
  "${BASEDIR}/mvnw" clean install $MAVEN_OPTS
  popd
}

function verifier() {
  pushd verifier
  "${BASEDIR}/mvnw" $MAVEN_CLEAN_GOAL install $MAVEN_OPTS
  popd
}

function runtime() {
  pushd runtime
  "${BASEDIR}/mvnw" $MAVEN_CLEAN_GOAL install $MAVEN_OPTS
  popd  
}

function rest() {
  pushd rest
  "${BASEDIR}/mvnw" $MAVEN_CLEAN_GOAL install $MAVEN_IMAGE_BUILD_GOAL $MAVEN_OPTS
  popd
}

function ui() {
  pushd ui
  yarn
  yarn ng build -- --aot --prod --progress=false
  if [ -z "$SKIP_IMAGE_BUILDS" ]; then
      if [ -n "$WITH_IMAGE_STREAMS" ]; then
          BC_DETAILS=`oc get bc | grep syndesis-ui || echo ""`
          if [ -z "$BC_DETAILS" ]; then
              cat docker/Dockerfile | oc new-build --dockerfile=- --to=syndesis/syndesis-ui:latest --strategy=docker $OC_OPTS
          fi
          tar -cvf archive.tar dist docker
          oc start-build -F --from-archive=archive.tar syndesis-ui $OC_OPTS
          rm archive.tar
      else
          docker build -t syndesis/syndesis-ui:latest -f docker/Dockerfile . | cat -
      fi
  fi
  popd
}

# ============================================================================
# Main loop

init_options

if [ -n "$HELP" ]; then
   displayHelp
   exit 0
fi

git submodule init
git submodule update

for module in $(modules_to_build)
do
  echo "=========================================================="
  echo "Building ${module} ...."
  echo "=========================================================="
  eval "${module}"
done
