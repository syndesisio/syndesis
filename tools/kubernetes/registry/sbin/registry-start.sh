#!/bin/bash

while getopts :h: option
do
  case "${option}"
  in
    h) HOST=$OPTARG;;
    \?) echo "Usage: $0 -h <hostname>"; exit ;;
  esac
done
shift $((OPTIND -1))

#
# Specify the host name of the new registry (same as that used to be the registry image)
#
if [ -z "${HOST}" ]; then
  echo "Error: No host name has been specified. Cannot execute..."
  exit 1
fi

CERT="${HOST}.cert.pem"
KEY="${HOST}.key.pem"

#
# Sets minikube's docker repository to local, allowing the
# newly built image to be deposited directly into it
#
echo "Starting the docker registry on minikube"
eval $(minikube docker-env)

docker run \
  -d -p 5000:5000 \
  --restart=always \
  --name k8-registry \
  --hostname=${HOST} \
  -e REGISTRY_HTTP_ADDR=0.0.0.0:5000 \
  -e REGISTRY_HTTP_TLS_CERTIFICATE=/etc/${HOST}/certs/${CERT} \
  -e REGISTRY_HTTP_TLS_KEY=/etc/${HOST}/private/${KEY} \
  syndesis/registry:latest

#
# Unset the docker repostiory
#
eval $(minikube docker-env -u)
