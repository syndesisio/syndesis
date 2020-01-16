#!/usr/bin/env bash

# Exit if any error occurs
# Fail on a single failed command in a pipeline (if supported)
set -o pipefail

# Fail on error and undefined vars (please don't use global vars, but evaluation of functions for return values)
set -eu

# Save global script args
ARGS=("$@")

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

print_exit() {
  echo $1
  exit 1
}

migrate_db() {
    local user=$(readopt --user)
    if [[ -z "$user" ]]; then
        print_exit "user is not defined"
    fi

    local pass=$(readopt --pass)
    if [[ -z "$pass" ]]; then
        print_exit "pass is not defined"
    fi

    local url=$(readopt --url)
    if [[ -z "$url" ]]; then
        print_exit "url is not defined"
    fi

    java -jar /opt/syndesis-cli.jar migrate \
         --url="jdbc:${url}" \
         --user="${user}" \
         --password="${pass}"
}

# =================================================================================
# Startup

# Include common utilities
source $(basedir)/common.sh

if [[ $(hasflag --verbose) ]]; then
    export PS4='+(${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'
    set -x
fi

migrate_db
