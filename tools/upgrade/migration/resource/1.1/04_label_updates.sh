#!/bin/bash
oc label $(oc get all -l 'syndesis.io/app=syndesis' -o name) --overwrite=true app=syndesis
oc label persistentvolumeclaim/syndesis-db --overwrite=true syndesis.io/type=infrastructure
