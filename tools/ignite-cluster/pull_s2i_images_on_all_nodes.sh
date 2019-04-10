#!/bin/bash

extract_hostname_from_node_labels() {
  local node=$1
  oc get $node -o jsonpath="{.metadata.labels.hostname}"
}

create_prefill_pod() {
  local pod=$1
  local hostname=$2
  local image=$3

  cat <<EOT | oc create -f -
apiVersion: v1
kind: Pod
metadata:
  name: $pod
spec:
  restartPolicy: Never
  containers:
  - name: s2i-pull
    image: ${image}
    command:
    - echo
  nodeSelector:
    hostname: ${hostname}
EOT
}

wait_for_pod_to_complete() {
  local pod=$1

  local status=$(oc get pod $pod -o jsonpath="{.status.phase}")
  while [ $status != "Succeeded" ]; do
      echo "Pod in status '$status': Sleeping 4s ..."
      sleep 4
      status=$(oc get pod $pod -o jsonpath="{.status.phase}")
  done
}

get_image_reference() {
  local version=$1

  oc get istag syndesis-s2i:$version -o jsonpath="{.image.dockerImageReference}"
}

node_type() {
  local node=$1
  oc get $1 -o jsonpath="{.metadata.labels.type}"
}

# S2I version number to prefetch
version=1.3.5

image=$(get_image_reference $version)
echo "Prepulling nodes with image $image"
for node in $(oc get nodes -o name); do
    echo "==== Prefilling $node"
    node_type=$(node_type $node)
    if [ $node_type == "compute" ]; then
        hostname=$(extract_hostname_from_node_labels $node)
        pod="s2i-prefill-$hostname"
        create_prefill_pod $pod $hostname $image
        wait_for_pod_to_complete $pod
        oc delete pod $pod
    else
        echo "Ignoring as node type is $node_type (not 'compute')"
    fi
done
