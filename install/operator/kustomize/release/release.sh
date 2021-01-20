#!/bin/bash

# Save global script args
ARGS=("$@")

# Exit if any error occurs
# Fail on a single failed command in a pipeline (if supported)
set -o pipefail

# Fail on error and undefined vars (please don't use global vars, but evaluation of functions for return values)
set -eu

# Helper functions

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
    for var in "${ARGS[@]:-}"; do
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
    for var in "${ARGS[@]:-}"; do
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

# Getting base dir
BASEDIR=$(basedir)
RELEASE_GIT_ORG="phantomjinx"
RELEASE_GIT_REPO="syndesis"
TAR_NAME="syndesis-install-release"

get_github_username() {
    if [ -z "${GITHUB_USERNAME:-}" ]; then
        echo "ERROR: environment variable GITHUB_USERNAME has not been set."
        echo "Please populate it with your github id"
        return
    fi
    echo $GITHUB_USERNAME
}

get_github_access_token() {
    if [ -z "${GITHUB_ACCESS_TOKEN:-}" ]; then
        echo "ERROR: environment variable GITHUB_ACCESS_TOKEN has not been set."
        echo "Please populate it with a valid personal access token from github (with 'repo', 'admin:org_hook' and 'admin:repo_hook' scopes)."
        return
    fi
    echo $GITHUB_ACCESS_TOKEN
}

check_error() {
  local msg="$*"
  if [ "${msg//ERROR/}" != "${msg}" ]; then
    echo $msg
    exit 1
  fi
}

publish_artifact() {
  if [ -z ${VERSION} ]; then
    check_error "Error: Version not defined"
  fi

  local release_file="${TAR_NAME}-${VERSION}"
  local github_url="https://api.github.com/repos/${RELEASE_GIT_ORG}/${RELEASE_GIT_REPO}/releases"

  set +e
  local upload_url=$(\
    curl -q --fail -X POST \
      -u $GITHUB_USERNAME:${GITHUB_ACCESS_TOKEN} \
      -H "Accept: application/vnd.github.v3+json" \
      -H "Content-Type: application/json" \
      -d "{\"tag_name\": \"${release_file}\"}" \
      ${github_url} | \
      jq -r .upload_url | cut -d{ -f1)

  if [[ ! $upload_url == http* ]]; then
    echo "ERROR: Cannot create release on remote github repository. Check if a release with the same tag already exists."
    return
  fi
  set -e

  if [ ! -f "${release_file}.tar.gz" ]; then
    echo "ERROR: Cannot find release tar archive in current directory"
    return
  fi

  set +e
  curl -q --fail -X POST -u ${GITHUB_USERNAME}:${GITHUB_ACCESS_TOKEN} \
    -H "Accept: application/vnd.github.v3+json" \
    -H "Content-Type: application/tar+gzip" \
    --data-binary "@${release_file}.tar.gz" \
    ${upload_url}?name="${release_file}.tar.gz"
  local err=$?
  set -e

  if [ $err -ne 0 ]; then
    echo "ERROR: Cannot upload release artifact ${release_file}.tar.gz on remote github repository"
    return
  fi
}

release_tar() {
  local github_username=$(get_github_username)
  check_error $github_username

  local github_token=$(get_github_access_token)
  check_error $github_token

  result=$(publish_artifact)
  check_error $result
}

release() {
  if [ -z ${VERSION} ]; then
    check_error "Error: Version not defined"
  fi

  local release_file="${TAR_NAME}-${VERSION}"

  echo "=== Tagging ${release_file}"
  git tag -f "${release_file}"

  # Push release tag
  local remote="git@github.com:${RELEASE_GIT_ORG}/${RELEASE_GIT_REPO}.git"
  git push ${remote} ${release_file}

  echo "==== Releasing tar file"
  release_tar
}


# ==========================================================================================

if [ "$#" -ne 1 ]; then
    echo "usage: $0 <version>"
    exit 1
fi

VERSION="${1}"
release
