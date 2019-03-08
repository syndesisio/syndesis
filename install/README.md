# Syndesis OpenShift Templates

This repository is about the canonical way to install Syndesis by using OpenShift templates for deploying on an OpenShift cluster.

## Installing Syndesis

The canonical way to install Syndesis is to use the mono script `syndesis` which in turn uses these templates to instantiate Syndesis on an OpenShift cluster.
`syndesis` is described in detail in the [Syndesis Developer Handbook](https://doc.syndesis.io/).

The following two sub-commands use the Syndesis templates from this directory:

* [syndesis install](https://doc.syndesis.io/#syndesis-install) - Install Syndesis to an already running OpenShift cluster
* [syndesis minishift](https://doc.syndesis.io/#syndesis-minishift) - Create a Syndesis instance on [Minishift](https://www.openshift.org/minishift/)

If you just want to install Syndesis, then please check the documentation referenced above.
The following sections explain the structure of these templates, which is only necessary to know when adapting the templates.

## Template flavors

There exist different flavours of OpenShift templates, with the following characteristics:

| Template | Description |
| -------- | ---------- |
| [syndesis.yml](https://raw.githubusercontent.com/syndesisio/syndesis/master/install/syndesis.yml) | Standard Syndesis template |
| [syndesis-dev.yml](https://raw.githubusercontent.com/syndesisio/syndesis/master/install/syndesis-dev.yml) | Same as above, but with debug enabled and allowing access from localhost (for local UI development) |

In order to apply the templates you can directly refer to the given files via its GitHub URL:

```bash
$ oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis.yml
```

All of these templates are generated from a single source [syndesis.yml.mustache](generator/syndesis.yml.mustache). So instead of editing individual descriptors you have to change this master template and then run `generator/run.sh`.

## Template parameters

All template parameters are required. Most of them have sane defaults, but some of them have not. These must be provided during instantiation with `oc new-app`


### Required input parameters

| Parameter | Description |
| --------- | ----------- |
| **ROUTE_HOSTNAME** | The external hostname to access Syndesis |

In order to one of the templates described above these parameters must be provided:

```
$ oc new-app --template=syndesis -p \
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
| **OPENSHIFT_CONSOLE_URL** | Optional URL to the OpenShift consol (e.g. https://console.123a.openshift.com/console) | |
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

If you want to use the development version which refers directly to Docker images substitute `syndesis` with `syndesis-dev` in the example above.

Once everything is running, you should be able to access Syndesis at https://syndesis.127.0.0.1.nip.io and log in with the OpenShift user `developer` using any password.

## Installing Syndesis

The recommended way is to use these templates with the `syndesis` tool as described in the [Syndesis Developer Handbook](https://doc.syndesis.io/#syndesis-install).

The following sections describe which steps `syndesis install` actually perform under the hood.

#### (Optional) Create a project

It is advisable to run the Syndesis in its own project so that it can adhere to cluster quotas:

```bash
$ oc new-project syndesis
```

#### Create service account to use as OAuth client

OpenShift includes the ability for a service account to act as a limited OAuthClient (see
[here](https://docs.openshift.org/latest/architecture/additional_concepts/authentication.html#service-accounts-as-oauth-clients)
for more details). Let's create the service account with the correct redirect URIs enabled:

```bash
$ oc create -f support/serviceaccount-as-oauthclient-restricted.yml
```

#### Using service account recognized by OpenShift

In order to bypass the authorization screen you can use OAuth account that is recognized by OpenShift.

First you need to create an `OAuthClient` resource, this can be done using a privileged OpenShift account, by default members of the `system:cluster-admins` are allowed this. You can get a list of the accounts that have this ability by running `oc policy who-can create OAuthClient`.

```yaml
apiVersion: oauth.openshift.io/v1
kind: OAuthClient
grantMethod: auto
metadata:
  name: ${CLIENT_ID}
redirectURIs:
- https://${ROUTE_HOSTNAME}/oauth/callback
secret: ${SECRET}
```

Where `${CLIENT_ID}` is unique string used to distinguish the particular Syndesis installation, `${ROUTE_HOSTNAME}` is the fully qualified hostname configured for the `syndesis` Route and `${SECRET}` is a sequence of random characters used to authenticate Syndesis requests to OpenShift.

Then change the `syndesis-oauthproxy` DeploymentConfig so that the `syndesis-oauthproxy` container arguments reference those that OAuth client ID and secret:

For example:
```yaml
apiVersion: apps.openshift.io/v1
kind: DeploymentConfig
metadata:
  name: syndesis-oauthproxy
spec:
  template:
    spec:
      containers:
      - args:
        - --client-id=${CLIENT_ID}
        - --client-secret=${SECRET}
```

#### Create the template to use

Create the template:

```bash
$ oc create -f syndesis.yml
```

For development purposes you can also chose `syndesis-dev.yml` which enables Java remote debugging by default and allows CORS access to a locally running UI proxy server.

#### Create the new app

You can now use the template and the ServiceAccount created above to deploy Syndesis:

```bash
$ oc new-app --template=syndesis \
    -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME> \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client) \
    -p INSECURE_SKIP_VERIFY=true
```

Replace `EXTERNAL_HOSTNAME` appropriately with your public Syndesis address (something like `syndesis.127.0.0.1.nip.io` works great if you are using `oc cluster up` locally).
Also, when using the development version, use 'syndesis-dev' as template name.
Optionally you can add a parameter `OPENSHIFT_CONSOLE_URL` which should be the base URL to the OpenShift console. If given, this is URL is used to calculate a link to the integration runtime's system log which can be found in the integration's activity page.

#### Log in

You should now be able to log in at `https://<EXTERNAL_HOSTNAME>`.

#### Create template for public API endpoint

Syndesis also provides a public API useful for external user defined Continuous Delivery pipelines for tagging and exporting/importing integrations across Syndesis clusters. 

Create the template:

```bash
$ oc create -f support/syndesis-public-oauth-proxy.yml
```

#### Create the new app for public API endpoint

You can now use the template `syndesis-public-oauthproxy` and the ServiceAccount created above to deploy Syndesis public API:

```bash
$ oc new-app --template=syndesis-public-oauthproxy \
    -p PUBLIC_API_ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME> \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client) \
    -p SAR_PROJECT=$(oc project -q)
```

Replace `EXTERNAL_HOSTNAME` appropriately with your public Syndesis address (something like `public-syndesis.127.0.0.1.nip.io` works great if you are using `oc cluster up` locally).
