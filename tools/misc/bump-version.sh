#!/bin/bash

if [ -z "${1}" ]; then
  echo "${0} <new-version>"
  exit 1
fi

VERSION="${1}"
SRC_ROOT="../.."

if [ ! -f "$(basename ${0})" ]; then
  echo "Please execute this script from its own directory"
  exit 1
fi

#######################################
#
# Upgrade this repository
#
#######################################

#
# Upgrade maven poms
#
APP_DIR="${SRC_ROOT}/app"
if [ -d "${APP_DIR}" ]; then
  echo "------"
  echo -n "Upgrading app directory poms ... "
  pushd "${SRC_ROOT}/app" > /dev/null
  for pom in pom.xml integration/bom/pom.xml integration/bom/pom.xml extension/bom/pom.xml; do
    ./mvnw -N versions:set -DgenerateBackupPoms=false -DnewVersion=$VERSION-SNAPSHOT -f ${pom} &> /dev/null
    if [ $? != 0 ]; then
      exit 1
    fi
  done
  echo "done"

  SYNDESIS_TEST_ENV="test/test-support/src/main/java/io/syndesis/test/SyndesisTestEnvironment.java"
  if [ -f "${SYNDESIS_TEST_ENV}" ]; then
    echo "------"
    echo -n "Upgrading ${SYNDESIS_TEST_ENV} ... "
    sed -i 's/\".*-SNAPSHOT/\"'${VERSION}'-SNAPSHOT/g' ${SYNDESIS_TEST_ENV}
    if [ $? != 0 ]; then
      exit 1
    else
      echo "done"
    fi
  fi
  popd > /dev/null
fi

#
# Upgrade makefile constant
#
VAR_MAKEFILE="${SRC_ROOT}/install/operator/config/vars/Makefile"
if [ -f "${VAR_MAKEFILE}" ]; then
  echo "------"
  echo -n "Upgrading ${VAR_MAKEFILE} ... "

  if ! grep "DEFAULT_VERSION := ${VERSION}" ${VAR_MAKEFILE} &> /dev/null; then
    PREV_VERSION=$(grep "DEFAULT_VERSION :=" ${VAR_MAKEFILE} | awk {'print $3'})
    sed -i "s/DEFAULT_VERSION :=.*/DEFAULT_VERSION := ${VERSION}.0/g" ${VAR_MAKEFILE}
    sed -i "s/DEFAULT_PREVIOUS_VERSION :=.*/DEFAULT_PREVIOUS_VERSION := ${PREV_VERSION}/g" ${VAR_MAKEFILE}
  fi

  echo "done"
fi

#
# Upgrade build config file for operator
#
OP_CONFIG="${SRC_ROOT}/install/operator/build/conf/config.yaml"
if [ -f ${OP_CONFIG} ]; then
  sed -i "s/Version: \".*\"/Version: \"${VERSION}\"/" ${OP_CONFIG}
fi

#
# Upgrade github workflow for daily releases
#
GIT_DAILY="${SRC_ROOT}/.github/workflows/daily_release.yml"
if [ -f ${GIT_DAILY} ]; then
  echo -n "Upgrading ${GIT_DAILY} ... "
  sed -i "s/- branch: .*\.x/- branch: ${VERSION}.x/" ${GIT_DAILY}
  echo "done"
fi
