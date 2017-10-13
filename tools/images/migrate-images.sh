#!/bin/bash

set -euo pipefail

FROM_REGISTRY=${1:-docker-registry.engineering.redhat.com/jboss-fuse-7-tech-preview}
TO_REGISTRY=${2:-registry.fuse-ignite.openshift.com/fuse-ignite}
SYNDESIS_VERSION=${3:-1.0.0}
IMAGES=${4:-" fuse-ignite-mapper:1.30.0 fuse-ignite-pemtokeystore:${SYNDESIS_VERSION} fuse-ignite-rest:${SYNDESIS_VERSION} fuse-ignite-ui:${SYNDESIS_VERSION} fuse-ignite-verifier:${SYNDESIS_VERSION}"}

# Push all images to target regitry
# Also push links to head versions (i.e. 1.0 -> 1.0.0)
for image in ${IMAGES}; do
  docker pull ${FROM_REGISTRY}/${image}
  docker tag ${FROM_REGISTRY}/${image} ${TO_REGISTRY}/${image}
  head_image=$(echo $image | sed -e 's/^\(.*\)\(\.[^.]*\)$/\1/')
  docker tag ${TO_REGISTRY}/${image} ${TO_REGISTRY}/${head_image}
  docker push ${TO_REGISTRY}/${image}
  docker push ${TO_REGISTRY}/${head_image}
done

# Push s2i builder image
S2I_IMAGE_SRC="registry.access.redhat.com/jboss-fuse-6/fis-java-openshift:2.0-9"
S2I_IMAGE_TRG="fuse-ignite-java-openshift:$SYNDESIS_VERSION"
docker pull ${S2I_IMAGE_SRC}
docker tag ${S2I_IMAGE_SRC} ${TO_REGISTRY}/${S2I_IMAGE_TRG}
head_s2i_image=$(echo $S2I_IMAGE_TRG | sed -e 's/^\(.*\)\(\.[^.]*\)$/\1/')
docker tag ${TO_REGISTRY}/$S2I_IMAGE_TRG ${TO_REGISTRY}/${head_s2i_image}
docker push ${TO_REGISTRY}/$S2I_IMAGE_TRG
docker push ${TO_REGISTRY}/${head_s2i_image}
