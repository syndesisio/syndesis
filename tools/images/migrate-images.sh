#!/bin/bash

set -euo pipefail

FROM_REGISTRY_PREFIX=${1:-docker-registry.engineering.redhat.com/jboss-fuse-7-tech-preview}
TO_REGISTRY_PREFIX=${2:-registry.fuse-ignite.openshift.com/fuse-ignite}
VERSION=${3:-1.0}
IMAGES=${4:-"fuse-ignite-java-openshift fuse-ignite-karaf-openshift fuse-ignite-mapper fuse-ignite-pemtokeystore fuse-ignite-rest fuse-ignite-token-rp fuse-ignite-ui fuse-ignite-verifier"}

for image in ${IMAGES}; do
  docker pull ${FROM_REGISTRY_PREFIX}/${image}:${VERSION}
  docker tag ${FROM_REGISTRY_PREFIX}/${image}:${VERSION} ${TO_REGISTRY_PREFIX}/${image}:${VERSION}
  docker push ${TO_REGISTRY_PREFIX}/${image}:${VERSION}
done
