#!/bin/bash
#
# Creats PVs for the ipaas deployment on minishift
#
set -e 

oc login -u system:admin

echo "
  sudo rm -rf /tmp/ipaas-db
  sudo mkdir -p /tmp/ipaas-db
  sudo chmod a+w /tmp/ipaas-db
  " | minishift ssh

cat <<EOF | oc replace --force -f -
kind: PersistentVolume
apiVersion: v1
metadata:
  name: ipaas-db
  labels:
    type: local
    app: redhat-ipaas
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/tmp/ipaas-db"
EOF

