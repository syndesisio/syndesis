# Red Hat iPaaS OpenShift Templates

This repository contains a simple way to get the Red Hat iPaaS deployed, using OpenShift templates,
on a running cluster.

Run the following commands:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas.yml
$ oc new-app redhat-ipaas -p ROUTE_HOSTNAME=<external hostname>
```

Replace _&lt;external hostname&gt;_ with a value that will resolve to the address of the OpenShift router.

You have to chose an address or _&lt;external hostname&gt;_ which is routable on your system (and also resolvable from inside your cluster). For a development setup you can use an external DNS resolving service like xip.io or nip.io:
Assuming that your OpenShift cluster is reachable under the IP address _ip_ then use `ipaas.`_ip_`.nip.io`.) (e.g. `ipass.127.0.0.1.nip.io` if your cluster is listening on localhost). With minishift you can retrieve the IP of the cluster with `minishift ip`.

Once all pods are started up, you should be able to access the iPaaS at `https://`_&lt;external hostname&gt;_`/`.

## Template parameters

* `ROUTE_HOSTNAME`: The external hostname to access the iPaaS
* `KEYCLOAK_ROUTE_HOSTNAME`: The external hostname to access the iPaaS Keycloak
* `KEYCLOAK_ADMIN_USERNAME`: The Keycloak admin username
* `KEYCLOAK_ADMIN_PASSWORD`: The Keycloak admin password
* `KEYCLOAK_IPAAS_REALM_NAME`: iPaaS Keycloak realm name
* `KEYCLOAK_IPAAS_REST_CLIENT_SECRET`: iPaaS REST service client secret
* `KEYCLOAK_ALLOW_ANY_HOSTNAME`: The Keycloack parameter to disable hostname validation on certificate
* `OPENSHIFT_MASTER`: Public OpenShift master address
* `OPENSHIFT_OAUTH_CLIENT_ID`: OpenShift OAuth client ID
* `OPENSHIFT_OAUTH_CLIENT_SECRET`: OpenShift OAuth client secret
* `OPENSHIFT_OAUTH_DEFAULT_SCOPES`: OpenShift OAuth default scopes
* `PEMTOKEYSTORE_IMAGE`: PEM to keystore init container image
* `IMAGE_PULL_POLICY`: ImagePullPolicy configuration on rhipaas images
* `GITHUB_OAUTH_CLIENT_ID` GitHub OAuth client ID
* `GITHUB_OAUTH_CLIENT_SECRET` GitHub OAuth client secret
* `GITHUB_OAUTH_DEFAULT_SCOPES` GitHub OAuth default scopes
* `POSTGRESQL_MEMORY_LIMIT` Maximum amount of memory the PostgreSQL container can use
* `POSTGRESQL_IMAGE_STREAM_NAMESPACE` The OpenShift Namespace where the PostgreSQL ImageStream resides
* `POSTGRESQL_USER` Username for PostgreSQL user that will be used for accessing the database
* `POSTGRESQL_PASSWORD` Password for the PostgreSQL connection user
* `POSTGRESQL_DATABASE` Name of the PostgreSQL database accessed
* `POSTGRESQL_VOLUME_CAPACITY` Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi

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

## Running single tenant

If you don't have cluster admin privileges, then you can run the iPaaS as a single tenant deployment
which only needs admin role in a project. This restricts all access to the single project and as such
acts as a single tenant. The drawback to this is of course that you need to deploy the iPaaS services
and pods into every project that you want to provision integrations in, but this is fine for a single,
local deployment.

Deployment is a bit more complicated because it requires a few extra steps to set stuff up:

### (Optional) Create a project

It is advisable to run the iPaaS in its own project so that it can adhere to cluster quotas:

```bash
$ oc new-project ipaas-single-tenant
```

### Create service account to use as OAuth client

OpenShift includes the ability for a service account to act as a limited OAuthClient (see
[here](https://docs.openshift.org/latest/architecture/additional_concepts/authentication.html#service-accounts-as-oauth-clients)
for more details). Let's create the service account with the correct redirect URIs enabled:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/serviceaccount-as-oauthclient-single-tenant.yml
```

### Create the template to use

We will create the template in the project, rather than in the openshift namespace as it is assumed
the user does not have cluster-admin rights:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas-dev-single-tenant.yml
```

### Create the new app

You can now use the template and the ServiceAccount created above to deploy the single tenant iPaaS:

```bash
$ oc new-app redhat-ipaas-dev-single-tenant \
    -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME> \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:$(oc project -q):ipaas-oauth-client \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client) \
    -p OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:$(oc project -q):\!"
```

Replace `EXTERNAL_HOSTNAME` appropriately with your public iPaaS address (something like `ipaas.127.0.0.1.nip.io` works great if you are using `oc cluster up` locally).

### Log in

You should be able to log in at `https://<EXTERNAL_HOSTNAME>`.
