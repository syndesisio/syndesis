#!/bin/bash

if [ -z "${1}" ]; then
  echo "${0} <new-version>"
  exit 1
fi

VERSION="${1}"

if [ ! -f "$(basename ${0})" ]; then
  echo "Please execute this script from its own directory"
  exit 1
fi

#######################################
#
# Upgrade this repository
#
#######################################

. ./bump-version-functions.sh

upgrade_app ${VERSION}
upgrade_config_makefile ${VERSION}
upgrade_build_config ${VERSION}
upgrade_github_workflow ${VERSION}
