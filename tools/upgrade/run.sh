#!/bin/bash

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

set -e

tag=$(readopt --tag -t)
if [ -z "$tag" ]; then
  tag="latest"
fi

login="oc login -u $(oc whoami) -p $(oc whoami -t) $(oc whoami --show-server)"

oc new-app --template syndesis-upgrade \
           --param=OC_LOGIN="$login" \
           --param=SYNDESIS_VERSION=$tag
