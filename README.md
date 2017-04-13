# Red Hat iPaaS OpenShift Templates

This repository is about the canonical way to install Red Hat iPaaS by using OpenShift templates for deploying on an OpenShift cluster.

There exist different flavours of OpenShift templates, with the following characteristics:

| Template | Descripton |
| -------- | ---------- |
| [ipaas.yml](https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas.yml) | Full production when setting up on a cluster with full access rights. Uses image streams under the hoods. |
| [ipaas-dev.yml](https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-dev.yml) | Same as above, but with direct references to Docker images so that they locally created images (e.g. against a Minishift Docker daemon) can be used directly. |
| [ipaas-restricted.yml](https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-restricted.yml) | If running in an restricted environment without admin access this template should be used. See the [section](#running-single-tenant) below for detailed usage instructions. |
| [ipaas-dev-restricted.yml](https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-dev-restricted.yml) | Same as above, but as a developer version with using direct Docker images |
| [ipaas-restricted-ephemeral.yml](https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-restricted-ephemeral.yml) | A variant of `ipaas-restricted.yml` which does only use temporary persistence. Mostly needed for testing as a workaround to the [pods with pvc sporadically timeout](https://bugzilla.redhat.com/show_bug.cgi?id=1435424) issue. |

More about the differences can be found in this [issue](https://github.com/redhat-ipaas/openshift-templates/issues/28)

In order to apply the templates you can directly refer to the given files via its GitHub URL:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas.yml
```

All of these templates are generated from a single source [ipaas.yml.mustache](generator/ipaas.yml.mustache). So instead of editing individual descriptors you have to change this master template and then run `generator/generate-templates.sh`.

## Template parameters

All template parameters are required. Most of them have sane defaults, but some of them have not. These must be provided during instantiation with `oc new-app`


### Required input parametes

| Parameter | Description |
| --------- | ----------- |
| **ROUTE_HOSTNAME** | The external hostname to access the iPaaS |
| **GITHUB_OAUTH_CLIENT_ID** | GitHub OAuth client ID |
| **GITHUB_OAUTH_CLIENT_SECRET** | GitHub OAuth client secret |

In order to one of the templates described above these parameters must be provided:

```
$ oc new-app ipaas -p \
       ROUTE_HOSTNAME=<external hostname> \
       GITHUB_OAUTH_CLIENT_ID=<oauth client> \
       GITHUB_OAUTH_CLIENT_SECRET=<secret>
```

Replace _&lt;external hostname&gt;_ with a value that will resolve to the address of the OpenShift router.

You have to chose an address or _&lt;external hostname&gt;_ which is routable on your system (and also resolvable from inside your cluster). For a development setup you can use an external DNS resolving service like xip.io or nip.io:
Assuming that your OpenShift cluster is reachable under the IP address _ip_ then use `ipaas.`_ip_`.nip.io`.) (e.g. `ipass.127.0.0.1.nip.io` if your cluster is listening on localhost). With minishift you can retrieve the IP of the cluster with `minishift ip`.

In order to use the GitHub integration you need a GitHub application registered at https://github.com/settings/developers For the registration, you will be asked for a _callback URL_. Please use the route you have given above: `https://<external hostname>` (e.g. `https://ipaas.127.0.0.1.nip.io`). GitHub will you then give a _client id_ and a _client secrte_ which you set for the corresponding template parameters.

Once all pods are started up, you should be able to access the iPaaS at `https://`_&lt;external hostname&gt;_`/`.

### Parameters with default values

| Parameter | Description | Default |
| --------- | ----------- | ------- |
| **KEYCLOAK_ROUTE_HOSTNAME** | The external hostname to access the iPaaS Keycloak | ipaas-keycloak.127.0.0.1.xip.io |
| **KEYCLOAK_ADMIN_USERNAME** |  The Keycloak admin username | admin |
| **KEYCLOAK_ADMIN_PASSWORD** | The Keycloak admin password | _(generated)_ |
| **KEYCLOAK_IPAAS_REALM_NAME** | iPaaS Keycloak realm name | ipaas |
| **KEYCLOAK_IPAAS_REST_CLIENT_SECRET** | iPaaS REST service client secret | _(generated)_ |
| **KEYCLOAK_ALLOW_ANY_HOSTNAME** | The Keycloack parameter to disable hostname validation on  certificate | false |
| **OPENSHIFT_MASTER** | Public OpenShift master address | https://localhost:8443 |
| **OPENSHIFT_OAUTH_CLIENT_ID** | OpenShift OAuth client ID | ipaas |
| **OPENSHIFT_OAUTH_CLIENT_SECRET** | OpenShift OAuth client secret | _(generated)_ |
| **OPENSHIFT_OAUTH_DEFAULT_SCOPES** | OpenShift OAuth default scopes | user:full |
| **PEMTOKEYSTORE_IMAGE** | PEM to keystore init container image | jimmidyson/pemtokeystore:v0.2.0 |
| **GITHUB_OAUTH_DEFAULT_SCOPES** | GitHub OAuth default scopes | user:email public_repo |
| **POSTGRESQL_MEMORY_LIMIT** | Maximum amount of memory the PostgreSQL container can use | 512Mi |
| **POSTGRESQL_IMAGE_STREAM_NAMESPACE** | The OpenShift Namespace where the PostgreSQL ImageStream resides | openshift |
| **POSTGRESQL_USER** | Username for PostgreSQL user that will be used for accessing the database | ipaas |
| **POSTGRESQL_PASSWORD** | Password for the PostgreSQL connection user | _(generated)_ |
| **POSTGRESQL_DATABASE** | Name of the PostgreSQL database accessed | ipaas |
| **POSTGRESQL_VOLUME_CAPACITY** | Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi | 1Gi |
| **INSECURE_SKIP_VERIFY** | Whether to skip the verification of SSL certificates for internal services | false |
| **TEST_SUPPORT_ENABLED** | Whether test support for e2e test is enabled | false |

## Running as a Cluster Admin

You can use either [Minishift](https://github.com/minishift/minishift) or [`oc cluster up`](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md) to setup your OpenShift system. For Minishift specific instructions see [below](#minishift-quickstart).

Once they are started and you have logged in with `oc login -u system:admin`, run:

```bash
$ oc create -n openshift -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas.yml
$ oc new-project ipaas
# Create app with the required params
$ oc new-app ipaas -p ROUTE_HOSTNAME=ipaas.127.0.0.1.nip.io -p GITHUB_CLIENT_ID=... -p GITHUB_CLIENT_SECRET=...
# Wait until all started
$ oc get pods -w
```

If you want to use the development version which refers directly to Docker images substitute `ipaas` with `ipaas-dev` in the example above.

Once everything is running, you should be able to access iPaaS at https://ipaas.127.0.0.1.nip.io and log in with the OpenShift user `developer` using any password.

## Running in a Restricted environment

If you don't have cluster admin privileges, then you can run the iPaaS as a single tenant deployment which only needs admin role in a project. This restricts all access to the single project and as such acts as a single tenant. The drawback to this is of course that you need to deploy the iPaaS services and pods into every project that you want to provision integrations in, but this is fine for a single, local deployment.

Deployment is a bit more complicated because it requires a few extra steps to set stuff up:

#### (Optional) Create a project

It is advisable to run the iPaaS in its own project so that it can adhere to cluster quotas:

```bash
$ oc new-project ipaas-restricted
```

#### Create service account to use as OAuth client

OpenShift includes the ability for a service account to act as a limited OAuthClient (see
[here](https://docs.openshift.org/latest/architecture/additional_concepts/authentication.html#service-accounts-as-oauth-clients)
for more details). Let's create the service account with the correct redirect URIs enabled:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/support/serviceaccount-as-oauthclient-restricted.yml
```

#### Create the template to use

We will create the template in the project, rather than in the openshift namespace as it is assumed the user does not have cluster-admin rights:

```bash
$ oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-dev-restricted.yml
```

#### Create the new app

You can now use the template and the ServiceAccount created above to deploy the restricted iPaaS for a single tenant iPaaS:

```bash
$ oc new-app ipaas-dev-restricted \
    -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME> \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_OAUTH_CLIENT_ID=system:serviceaccount:$(oc project -q):ipaas-oauth-client \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token ipaas-oauth-client) \
    -p OPENSHIFT_OAUTH_DEFAULT_SCOPES="user:info user:check-access role:edit:$(oc project -q):! role:system:build-strategy-source:$(oc project -q)"
```

Replace `EXTERNAL_HOSTNAME` appropriately with your public iPaaS address (something like `ipaas.127.0.0.1.nip.io` works great if you are using `oc cluster up` locally).

#### Log in

You should be able to log in at `https://<EXTERNAL_HOSTNAME>`.

## Minishift Quickstart

With minishift you can easily try out redhat-ipaas. The only prerequisite is that you have a GitHub application registered at https://github.com/settings/developers For the registration, please use as callback URL the output of `https://ipaas.$(minishift ip).xip.io`. Then you get a `<GITHUB_CLIENT_ID>` and a `<GITHUB_CLIENT_SECRET>`. These should be used in the commands below.

### Template selection

The template to use in the installation instructions depend on your use case:

* **Developer** : Use the template `ipaas-dev` which directly references Docker images without image streams. Then when before building you images e.g. with `mvn fabric8:build` set your `DOCKER_HOST` envvar to use the Minishift Docker daemon via `eval $(minishift docker-env)`. After you have created a new image you simply only need to kill the appropriate pod so that the new pod spinning up will use the freshly created image. 

* **Tester** / **User** : In case you only want to have the latest version if ipaas on your local Minishift installation, use the template `ipaas` which uses image stream refering to the published Docker Hub images. Minishift will update its images and trigger a redeployment when the images at Docker Hub changes. Therefor it checks every 15 minutes for a change image. You do not have to do anything to get your application updated except for waiting on Minishift to pick up ew images.

Depending on your role please use the appropriate template in the instructions below.

### Install instructions
> Please note that there is currently a switch for Minishift with regard to the default DNS reflector. For Minishift 1.0.0-rc1 please use `xip.io` as the domain. For Minishift 1.0.0-rc1 you have to use `nip.io` but you have also have to use the parameter `INSECURE_SKIP_VERIFY=true` because the internal certs still refer to `xip.io`. This should be fixed in the final 1.0.0 version of Minishift.


```bash
# Fire up minishift if not alread running. Please note that we need v1.5.0 right now
# for auto creating volumes. Alternatively you could use the provided script tools/create-pv-minishift.sh
# to create the PV on your own. Also, you need to add some memory, 4192 or more is recommended
minishift start  --openshift-version=v1.5.0-rc.0 --memory 4192

# Login as admin
oc login -u system:admin

# Register a GitHub application at https://github.com/settings/developers
# .....

# Use the result of this command as callback URL for the GitHub registration:
echo https://ipaas.$(minishift ip).xip.io

# Set your GitHub credentials
GITHUB_CLIENT_ID=....
GITHUB_CLIENT_SECRET=....

# Install the OpenShift template (ipaas-dev.yml or ipaas.yml)
oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/ipaas-dev.yml

# Create an App. Add the propert GitHub credentials. Use "ipaas-dev" or "ipaas" depending on the template
# you have installed
oc new-app ipaas-dev \
    -p ROUTE_HOSTNAME=ipaas.$(minishift ip).xip.io \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p GITHUB_OAUTH_CLIENT_ID=${GITHUB_CLIENT_ID} \
    -p GITHUB_OAUTH_CLIENT_SECRET=${GITHUB_CLIENT_SECRET} \
    -p INSECURE_SKIP_VERIFY=true

# Wait until all pods are running. Some pods are crashing at first, but are restarted
# so that the system will eventually converts to a stable state ;-). Especially the proxies
# need up to 5 restarts
watch oc get pods

# Open browser pointing ot the app
open https://ipaas.$(minishift ip).xip.io
```
