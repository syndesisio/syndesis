#!/bin/bash

SRC_ROOT="../.."

#
# Upgrade app maven poms
#
upgrade_app() {
  local version=${1:-}
  if [ -z "${version}" ]; then
    echo "Cannot upgrade app directory. Version is empty"
    exit 1
  fi

  local appdir="${SRC_ROOT}/app"
  if [ -d "${appdir}" ]; then
    echo "------"
    echo -n "Upgrading app directory poms ... "
    pushd "${SRC_ROOT}/app" > /dev/null
    for pom in pom.xml integration/bom/pom.xml integration/bom/pom.xml extension/bom/pom.xml; do
      oout=$(./mvnw -N versions:set -DgenerateBackupPoms=false -DnewVersion=$version-SNAPSHOT -f ${pom})
      if [ $? != 0 ]; then
        echo "Error: Please see maven output below:"
        echo ${oout}
        exit 1
      fi
    done
    echo "done"

    local syndesis_test_env="test/test-support/src/main/java/io/syndesis/test/SyndesisTestEnvironment.java"
    if [ -f "${syndesis_test_env}" ]; then
      echo "------"
      echo -n "Upgrading ${syndesis_test_env} ... "
      sed -i 's/\".*-SNAPSHOT/\"'${version}'-SNAPSHOT/g' ${syndesis_test_env}
      if [ $? != 0 ]; then
        echo "Failed"
        exit 1
      else
        echo "done"
      fi
    fi
    popd > /dev/null
  fi
}

#
# Upgrade makefile constant
#
upgrade_config_makefile() {
  local version=${1:-}
  if [ -z "${version}" ]; then
    echo "Cannot upgrade config makefile. Version is empty"
    exit 1
  fi

  local var_makefile="${SRC_ROOT}/install/operator/config/vars/Makefile"
  if [ -f "${var_makefile}" ]; then
    echo "------"
    echo -n "Upgrading ${var_makefile} ... "

    if ! grep "DEFAULT_VERSION := ${version}" ${var_makefile} &> /dev/null; then
      local prev_version=$(grep "DEFAULT_VERSION :=" ${var_makefile} | awk {'print $3'})
      sed -i "s/DEFAULT_VERSION :=.*/DEFAULT_VERSION := ${version}.0/g" ${var_makefile}
      sed -i "s/DEFAULT_PREVIOUS_VERSION :=.*/DEFAULT_PREVIOUS_VERSION := ${prev_version}/g" ${var_makefile}
    fi

    echo "done"
  fi
}

#
# Upgrade build config file for operator
#
upgrade_build_config() {
  local version=${1:-}
  if [ -z "${version}" ]; then
    echo "Cannot upgrade config makefile. Version is empty"
    exit 1
  fi

  local op_config="${SRC_ROOT}/install/operator/build/conf/config.yaml"
  if [ -f ${op_config} ]; then
    echo "------"
    echo -n "Upgrading ${op_config} ... "
    sed -i "s/Version: \".*\"/Version: \"${version}\"/" ${op_config}
    echo "done"
  fi
}

#
# Upgrade github workflow for daily releases
#
upgrade_github_workflow() {
  local version=${1:-}
  if [ -z "${version}" ]; then
    echo "Cannot upgrade config makefile. Version is empty"
    exit 1
  fi

  local git_daily="${SRC_ROOT}/.github/workflows/daily_release.yml"
  if [ -f ${git_daily} ]; then
    echo "------"
    echo -n "Upgrading ${git_daily} ... "
    sed -i "s/- branch: .*\.x/- branch: ${version}.x/" ${git_daily}
    echo "done"
  fi
}
