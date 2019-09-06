#!/bin/bash
# Updates fuse-ignite-s2i image stream tag
#S2I_TAG=${ENV_S2I_TARGET_TAG:-}
#
#if [ -z "$S2I_TAG" ]
#then
#	echo "Unable to fetch target value of s2i tag"
#	exit 0
#fi
#
#APP_PATCH=$(oc get configmap syndesis-server-config -o json -o "jsonpath={.data['application\.yml']}" | sed 's|builderImageStreamTag: .*$|builderImageStreamTag:'$S2I_TAG'|' )
#
#oc patch configmap syndesis-server-config -p "{ \"data\": { \"application.yml\": \"$APP_PATCH\" } }"
