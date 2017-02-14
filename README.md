# Red Hat iPaaS OpenShift Templates

This repository contains a simple way to get the Red Hat iPaaS deployed, using OpenShift templates,
on a running cluster.

Run the following commands:

```bash
oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas.yml
oc new-app redhat-ipaas -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME>
```

Replace `EXTERNAL_HOSTNAME` with a value that will resolve to the address of the OpenShift router.

Once all pods are started up, you should be able to access the iPaaS at `https://<EXTERNAL_HOSTNAME>/`.

## Template parameters

* `ROUTE_HOSTNAME`: The external hostname to access the iPaaS
* `KEYCLOAK_ROUTE_HOSTNAME`: The external hostname to access the iPaaS Keycloak
* `KEYCLOAK_ADMIN_USERNAME`: The Keycloak admin username
* `KEYCLOAK_ADMIN_PASSWORD`: The Keycloak admin password
* `KEYCLOAK_IPAAS_REALM_NAME`: iPaaS Keycloak realm name
* `KEYCLOAK_IPAAS_REST_CLIENT_SECRET`: iPaaS REST service client secret
* `OPENSHIFT_MASTER`: Public OpenShift master address
* `OPENSHIFT_OAUTH_CLIENT_ID`: OpenShift OAuth client ID
* `OPENSHIFT_OAUTH_CLIENT_SECRET`: OpenShift OAuth client secret
* `OPENSHIFT_OAUTH_DEFAULT_SCOPES`: OpenShift OAuth default scopes
* `PEMTOKEYSTORE_IMAGE`: PEM to keystore init container image

Only `ROUTE_HOSTNAME` is required, others have sane defaults that work for most cases.

## Running locally for development

Use either [Minishift](https://github.com/minishift/minishift) or [`oc cluster up`](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md).

Once they are started and you have logged in with `oc login -u system:admin`, run:

```bash
$ oc create -n openshift -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas.yml
$ oc new-project ipaas
$ oc new-app redhat-ipaas
# Wait until all started
$ oc get pods -w
```

Once everything is running, you should be able to access iPaaS at https://ipaas.127.0.0.1.nip.io and
log in with the OpenShift user `developer` using any password.
