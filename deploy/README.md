# Syndesis OpenShift Templates

This repository is about the canonical way to install Syndesis by using OpenShift templates for deploying on an OpenShift cluster.

There exist different flavours of OpenShift templates, with the following characteristics:

| Template | Descripton |
| -------- | ---------- |
| [syndesis.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis.yml) | Full production when setting up on a cluster with full access rights. Uses image streams under the hoods. |
| [syndesis-dev.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-dev.yml) | Same as above, but with direct references to Docker images so that they locally created images (e.g. against a Minishift Docker daemon) can be used directly. |
| [syndesis-restricted.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-restricted.yml) | If running in an restricted environment without admin access this template should be used. See the [section](#running-in-a-restricted-environment) below for detailed usage instructions. |
| [syndesis-dev-restricted.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-dev-restricted.yml) | Same as above, but as a developer version with using direct Docker images |
| [syndesis-restricted-ephemeral.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-restricted-ephemeral.yml) | A variant of `syndesis-restricted.yml` which does only use temporary persistence. Mostly needed for testing as a workaround to the [pods with pvc sporadically timeout](https://bugzilla.redhat.com/show_bug.cgi?id=1435424) issue. |
| [syndesis-ci.yml](https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-ci.yml) | A variant of `syndesis.yml` which makes limit use of probes. Mostly needed for testing as a workaround to the [http readiness and liveness probe fail](https://bugzilla.redhat.com/show_bug.cgi?id=1457399) issue. |

More about the differences can be found in this [issue](https://github.com/syndesisio/syndesis-openshift-templates/issues/28)

In order to apply the templates you can directly refer to the given files via its GitHub URL:

```bash
$ oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis.yml
```

All of these templates are generated from a single source [syndesis.yml.mustache](generator/syndesis.yml.mustache). So instead of editing individual descriptors you have to change this master template and then run `generator/run.sh`.

## Template parameters

All template parameters are required. Most of them have sane defaults, but some of them have not. These must be provided during instantiation with `oc new-app`


### Required input parametes

| Parameter | Description |
| --------- | ----------- |
| **ROUTE_HOSTNAME** | The external hostname to access Syndesis |

In order to one of the templates described above these parameters must be provided:

```
$ oc new-app syndesis -p \
       ROUTE_HOSTNAME=<external hostname>
```

Replace _&lt;external hostname&gt;_ with a value that will resolve to the address of the OpenShift router.

You have to chose an address or _&lt;external hostname&gt;_ which is routable on your system (and also resolvable from inside your cluster). For a development setup you can use an external DNS resolving service like xip.io or nip.io:
Assuming that your OpenShift cluster is reachable under the IP address _ip_ then use `syndesis.`_ip_`.nip.io`.) (e.g. `syndesis.127.0.0.1.nip.io` if your cluster is listening on localhost). With minishift you can retrieve the IP of the cluster with `minishift ip`.

Once all pods are started up, you should be able to access the Syndesis at `https://`_&lt;external hostname&gt;_`/`.

### Parameters with default values

| Parameter | Description | Default |
| --------- | ----------- | ------- |
| **OPENSHIFT_MASTER** | Public OpenShift master address | https://localhost:8443 |
| **OPENSHIFT_OAUTH_CLIENT_ID** | OpenShift OAuth client ID | syndesis |
| **OPENSHIFT_OAUTH_CLIENT_SECRET** | OpenShift OAuth client secret | _(generated)_ |
| **OPENSHIFT_OAUTH_DEFAULT_SCOPES** | OpenShift OAuth default scopes | user:full |
| **POSTGRESQL_MEMORY_LIMIT** | Maximum amount of memory the PostgreSQL container can use | 512Mi |
| **POSTGRESQL_IMAGE_STREAM_NAMESPACE** | The OpenShift Namespace where the PostgreSQL ImageStream resides | openshift |
| **POSTGRESQL_USER** | Username for PostgreSQL user that will be used for accessing the database | syndesis |
| **POSTGRESQL_PASSWORD** | Password for the PostgreSQL connection user | _(generated)_ |
| **POSTGRESQL_DATABASE** | Name of the PostgreSQL database accessed | syndesis |
| **POSTGRESQL_VOLUME_CAPACITY** | Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi | 1Gi |
| **INSECURE_SKIP_VERIFY** | Whether to skip the verification of SSL certificates for internal services | false |
| **TEST_SUPPORT_ENABLED** | Whether test support for e2e test is enabled | false |
| **DEMO_DATA_ENABLED** | Whether demo data is automatically imported on startup | true |
| **SYNDESIS_REGISTRY** | Registry from where to fetch Syndesis images | docker.io |
| **CONTROLLERS_INTEGRATION_ENABLED**  | Should deployment of integrations be enabled? | true |
| **SYNDESIS_ENCRYPT_KEY** | The encryption key used to encrypt/decrypt stored secrets | _(generated)_ |

## Running as a Cluster Admin

You can use either [Minishift](https://github.com/minishift/minishift) or [`oc cluster up`](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md) to setup your OpenShift system. For Minishift specific instructions see [below](#minishift-quickstart).

Once they are started and you have logged in with `oc login -u system:admin`, run:

```bash
$ oc create -n openshift -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis.yml
$ oc new-project syndesis
# Create app with the required params
$ oc new-app syndesis -p ROUTE_HOSTNAME=syndesis.127.0.0.1.nip.io
# Wait until all started
$ oc get pods -w
```

If you want to use the development version which refers directly to Docker images substitute `syndesis` with `syndesis-dev` in the example above.

Once everything is running, you should be able to access Syndesis at https://syndesis.127.0.0.1.nip.io and log in with the OpenShift user `developer` using any password.

## Running in a Restricted environment

If you don't have cluster admin privileges, then you can run the Syndesis as a single tenant deployment which only needs admin role in a project. This restricts all access to the single project and as such acts as a single tenant. The drawback to this is of course that you need to deploy the Syndesis services and pods into every project that you want to provision integrations in, but this is fine for a single, local deployment.

Deployment is a bit more complicated because it requires a few extra steps to set stuff up:

#### (Optional) Create a project

It is advisable to run the Syndesis in its own project so that it can adhere to cluster quotas:

```bash
$ oc new-project syndesis-restricted
```

#### Create service account to use as OAuth client

OpenShift includes the ability for a service account to act as a limited OAuthClient (see
[here](https://docs.openshift.org/latest/architecture/additional_concepts/authentication.html#service-accounts-as-oauth-clients)
for more details). Let's create the service account with the correct redirect URIs enabled:

```bash
$ oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/support/serviceaccount-as-oauthclient-restricted.yml
```

#### Create the template to use

We will create the template in the project, rather than in the openshift namespace as it is assumed the user does not have cluster-admin rights:

```bash
$ oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-dev-restricted.yml
```

#### Create the new app

You can now use the template and the ServiceAccount created above to deploy the restricted Syndesis for a single tenant Syndesis:

```bash
$ oc new-app syndesis-dev-restricted \
    -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME> \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client) \
    -p INSECURE_SKIP_VERIFY=true
```

Replace `EXTERNAL_HOSTNAME` appropriately with your public Syndesis address (something like `syndesis.127.0.0.1.nip.io` works great if you are using `oc cluster up` locally).

#### Log in

You should be able to log in at `https://<EXTERNAL_HOSTNAME>`.

## Minishift Quickstart

With Minishift you can easily try out Syndesis.

### Template selection

The template to use in the installation instructions depend on your use case:

* **Developer** : Use the template `syndesis-dev` or `syndesis-dev-restricted` which directly references Docker images without image streams. The _restricted_ variant should be used when running in an OpenShift environment where you don't have or don't want to use admin access. Then when before building you images e.g. with `mvn fabric8:build` set your `DOCKER_HOST` envvar to use the Minishift Docker daemon via `eval $(minishift docker-env)`. After you have created a new image you simply only need to kill the appropriate pod so that the new pod spinning up will use the freshly created image.

* **Tester** / **User** : In case you only want to have the latest version of Syndesis on your local Minishift installation, use the template `syndesis` which uses image stream refering to the published Docker Hub images. Minishift will update its images and trigger a redeployment when the images at Docker Hub changes. Therefore it checks every 15 minutes for a change image. You do not have to do anything to get your application updated except for waiting on Minishift to pick up new images.

Depending on your role please use the appropriate template in the instructions below.

### Install instructions

Here are step-by-step the installation instructions for setting up a Minishift installation in an restricted OpenShift environment:

```bash
# Fire up minishift if not alread running.
# 4 MB of memories are recommended
minishift start --memory 4192

# Add a serviceaccount as OAuth client to OpenShift
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/support/serviceaccount-as-oauthclient-restricted.yml

# Install the OpenShift template
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-dev-restricted.yml

# Create an App. Use "syndesis-dev" or "syndesis" depending on the template
# you have installed
oc new-app syndesis-dev-restricted \
    -p ROUTE_HOSTNAME=syndesis.$(minishift ip).nip.io \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server)

# Wait until all pods are running. Some pods are crashing at first, but are restarted
# so that the system will eventually converts to a stable state ;-). Especially the proxies
# need up to 5 restarts
watch oc get pods

# Open browser pointing ot the app
open https://syndesis.$(minishift ip).nip.io
```
