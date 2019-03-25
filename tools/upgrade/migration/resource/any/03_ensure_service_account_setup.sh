#!/bin/bash

# Check that all service accounts are created and link them to a given
# pull secret (if found).

# Please note that "syndesis-pull-secret" must exists *BEFORE* the update
# is performed. Otherwise there will no linkage

# If already logged in via `docker login` to registry.redhat.io,
# you can create this secret with

#  oc create secret generic syndesis-pull-secret --from-file=.dockerconfigjson=$HOME/.docker/config.json --type=kubernetes.io/dockerconfigjson

set +e

# Functions that are called at the end
# ==============================================================
function ensure_sa_syndesis_default_exists() {
  # Check for missing service account and add them
  if ! $(exists_resource sa syndesis-default); then
    echo "       ### Creating SA syndesis-default"
    oc create serviceaccount syndesis-default
  fi
}

function ensure_sas_are_linked_to_registry_secret_if_present() {
  # If pull secret "syndesis-pull-secret" exists, link them to a list of service accounts
  if $(exists_resource secret syndesis-pull-secret); then
    # Add pull secrets to every service account specific to syndesis
    for sa in $(oc get sa -o jsonpath='{.items[*].metadata.name}'); do
      # For all sa's which start with "syndesis-" + the camel k operator
      if [ ${sa/#syndesis-/} != $sa ] || [ $sa == "camel-k-operator" ]; then
        echo "       ### Linking syndesis-pull-secret --> SA $sa (pull)"
        oc secrets link $sa syndesis-pull-secret --for=pull
      fi
    done

    # Add pull and push secrets to the builder account
    echo "       ### Linking syndesis-pull-secret --> SA builder (pull + mount)"
    oc secrets link builder syndesis-pull-secret --for=pull,mount
  fi
}

function exists_resource() {
  local kind=$1
  local name=$2
  oc get $kind $name -o name >/dev/null 2>&1
  if [ $? != 0 ]; then
    echo "false"
  else
    echo "true"
  fi
}

# ==========================================================================

# Ensure SAs and linked secrets
ensure_sa_syndesis_default_exists
ensure_sas_are_linked_to_registry_secret_if_present
