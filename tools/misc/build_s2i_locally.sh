#!/bin/bash
#
# Build s2i image locally and push it to minishift registry afterwards
# 
# Ussage: 
#   - ./build_s2i_locally.sh --maven-mirror http://nexus-syndesis.nip.io/nexus/content/groups/public
#   - ./build_s2i_locally.sh

function print_exit {
  echo $1 && exit 1
}

OPENSHIFT_USER=developer
DEPENDENCIES='oc docker syndesis minishift'

for d in $DEPENDENCIES; do
  which $d > /dev/null 2>&1 || print_exit "No binary found for $d"
done

eval $(minishift docker-env)
syndesis -m s2i -i -f --docker $@

docker login -u ${OPENSHIFT_USER} -p $(oc whoami -t) $(minishift openshift registry)
docker tag syndesis/syndesis-s2i $(minishift openshift registry)/syndesis/syndesis-s2i
docker push $(minishift openshift registry)/syndesis/syndesis-s2i
