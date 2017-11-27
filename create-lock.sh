#!/bin/bash

#
# This is a samples script that can be used to create all the necessary locks that can be used for handling a project pool.
#
# Usage: create-lock.sh <project prefix>

PREFIX=$1
SERVICE_ACCOUNT="default"

function find_secret() {
	local NAMESPACE=$1
	local SERVICE_ACCOUNT=$2
	oc get sa $SERVICE_ACCOUNT -n $NAMESPACE -o yaml | grep $SERVICE_ACCOUNT-token- | awk -F ": " '{print $2}'
}

function read_token() {
	local SECRET=$1
	local NAMESPACE=$2
	oc get secret $SECRET -n $NAMESPACE -o yaml | grep token: | awk -F ": " '{print $2}' | base64 -d
}

function read_token_of_sa() {
	local NAMESPACE=$1
	local SERVICE_ACCOUNT=$2
	local SECRET=$(find_secret $NAMESPACE $SERVICE_ACCOUNT)
	local TOKEN=$(read_token $SECRET $NAMESPACE)
	echo $TOKEN
}


for p in `oc get projects | grep $PREFIX | awk -F " " '{print $1}'`; do
	echo "Creating a secret lock for project $p"
	SECRET=$(find_secret $p "default")
	echo "Found secret: $SECRET"
	TOKEN=$(read_token_of_sa $p $SERVICE_ACCOUNT)
	echo "Found token: $TOKEN"
	oc delete secret project-lock-$p || true
	oc create secret generic project-lock-$p --from-literal=token=$TOKEN 
	oc annotate secret project-lock-$p syndesis.io/lock-for-project=$p
	oc annotate secret project-lock-$p syndesis.io/allocated-by=""

	oc adm policy add-role-to-user edit system:serviceaccount:$p:$SERVICE_ACCOUNT -n $p
	oc adm policy add-role-to-user system:image-puller system:serviceaccount:$p:$SERVICE_ACCOUNT -n $p
	oc adm policy add-role-to-user system:image-builder system:serviceaccount:$p:$SERVICE_ACCOUNT -n $p
done
