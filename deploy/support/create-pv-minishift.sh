#!/bin/bash
#
# Creats PVs for the syndesis deployment on minishift
#
set -e

oc login -u system:admin

echo "
  sudo rm -rf /tmp/syndesis-db
  sudo mkdir -p /tmp/syndesis-db
  sudo chmod a+w /tmp/syndesis-db
  " | minishift ssh

cat <<EOF | oc replace --force -f -
kind: PersistentVolume
apiVersion: v1
metadata:
  name: syndesis-db
  labels:
    type: local
    app: syndesis
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Recycle
  hostPath:
    path: "/tmp/syndesis-db"
EOF
