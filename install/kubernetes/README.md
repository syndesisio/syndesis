# Syndesis on Minikube (Development)

Table of contents
=================

* [Table of contents](#table-of-contents)
* [Development Limitations](#development-limitations-)
* [Variables](#variables-)
* [Starting Minikube](#starting-minikube-)
  * [Enable Minikube Addons](#enable-minikube-addons-)
* [Configuring an Image Registry](#configuring-an-image-registry-)
  * [Overview](#overview-)
  * [Using quay.io](#using-quayio-)
  * [Using Minikube's Docker](#using-minikubes-docker-)
  * [Using a Local Registry](#using-a-local-registry-)
* [Building the Operator Image](#building-the-operator-image-)
* [Installing the Operator Image](#installing-the-operator-image-)
  * [Understanding Secrets](#understanding-secrets-)
  * [Executing The Install](#executing-the-install-)
    * [Preferred Command](#preferred-command-)
    * [Manual Commands](#manual-commands-) (Troubleshooting)
* [Accessing Syndesis Application](#accessing-syndesis-application-) 
* [Individual Task Commands](#individual-task-commands-)
  * [Creating a User](#creating-a-user-)
  * [Creating or Switching a Namespace](#creating-or-switching-a-namespace-)
  * [Re-creating a Namespace](#re-creating-a-namespace-)
  * [Displaying Cluster Name or URL](#displaying-cluster-name-or-url-)
  * [List Configured Contexts](#list-configured-contexts-)
  * [Switch Context](#switch-context-)
  * [Generating Syndesis Secrets](#generating-syndesis-secrets-)
  * [Starting a Local Docker Secure Registry](#starting-a-local-docker-secure-registry-)
  * [Creating Minikube Volumes](#creating-minikube-volumes-)
  

Development Limitations [&#8593;](#table-of-contents)
============
An installation of Syndesis on Minikube is seen as an ongoing developmental effort, ie. not ready for production. There remains a significant missing piece, namely the runtime for generated integrations. This is being worked on.

Variables [&#8593;](#table-of-contents)
=====
* **${SYNDESIS}**: this denotes the syndesis install binary, ie. ${syndesis download directory}/tools/bin/syndesis

Starting Minikube [&#8593;](#table-of-contents)
=========
* Download and install Minikube by following the instructions [here](https://kubernetes.io/docs/tasks/tools/install-minikube);
* Execute the syndesis command:
```
${SYNDESIS} minikube start
```
Optional Switch | Usage
------------ | -------------
``--reset`` | destroy the current Minikube VM before creating a new one
``--driver`` | the driver to use (kvm2 by default)
``--disk`` | allocate the size of storage (60GB by default)
``--memory`` | allocate the amount of memory (12GB by default)
``--cpus`` | allocate the number of CPUs (4 by default)
``--registries <file>`` | provide path to a file containing a list of registry urls that Minikube can access

### Enable Minikube addons [&#8593;](#table-of-contents)
Some addons are required and useful for running Syndesis on Minikube. The `start` script takes case of enabling the following addons:

* dashboard (optional but useful for later development)
* ingress
* ingress-dns

Configuring an Image Registry [&#8593;](#table-of-contents)
===============

### Overview [&#8593;](#table-of-contents)
While Syndesis provides the tools for building the Syndesis operator image, it is important to understand how that image is processed and made available to a kubernetes cluster. In Openshift, it is possible to directly upload the source-code of the operator and have it built on the cluster, using a feature called source-2-image. Once the image is built, it is pushed to Openshift's own registry to be accessible for deployment.

Minikube does not include source-2-image and hence expects to pull and deploy images directly. The registry used can be implemented in a variety of ways. However, the following describes the most workable approaches.

### Using quay.io [&#8593;](#table-of-contents)
The container registry [quay.io](https://quay.io) is a remote registry where images can either be built from source repositories or directly pushed using docker. To enable this:
* follow the [tutorial](https://quay.io/tutorial) to register;
* configure docker to access quay.io using `docker login`;
* create the **empty** repository `syndesis-operator` which will then be accessed by the full URL - _quay.io/${user}/syndesis-operator_

### Using Minikube's Docker [&#8593;](#table-of-contents)
When Minikube uses images, it pulls them into its own docker repository. It is possible to shortcut this process by tagging the image appropriately then pushing it directly into the docker repository. Therefore, when Syndesis requires the image, no pulling is required as its already available.

#### Using docker-env
Using the [docker-env](https://minikube.sigs.k8s.io/docs/commands/docker-env), command, retarget Minikube's docker daemon as the local one. Then when a build of the operator is performed, the image is actually built in Minikube's own repository. The default image _name:tag_ is _docker.io/syndesis/syndesis-operator_ so this is what the image should be tagged with in the docker repository and what the deployment configuration should reference the image as. If doing frequent image builds, this could represent the most efficient method of development.

### Using a Local Registry [&#8593;](#table-of-contents)
The registry does not have to be an established public service like [quay.io](quay.io) or [docker.io](docker.io). The infrastructure for generating a simple private registry is readily available and this can be preferable, especially if one wishes to avoid uploading to the internet. However, an important caveat is that Minikube (and Openshift) prefer registries that are secure, ie. signed by their own Certificate-Authority (CA) certificate and accessed via _https_. Registries that do not conform to these requirements are considered untrusted and so insecure.

If it is considered sufficient to allow insecure registery access then this can be [configured](https://minikube.sigs.k8s.io/docs/handbook/registry/#enabling-insecure-registries) on Minikube. Currently, Syndesis only provides tools for configuring a secure registry.

>Note.
Minikube does include a registry-addon but it is currently insecure and
has to be accessed using `localhost`. After some experimentation, this
was dropped in favour of setting up a private secure registry in docker.

#### Registry Hostname
A hostname should be chosen and assigned to **REGHOST**. This is a hostname that points to an IP address, accessible by Minikube and will be used in registry-access commands, eg. `https://${REGHOST}:5000/v2/_catalog`. This hostname will be assigned as the CN value to a Minikube-CA-signed TLS certificate allowing the registry to be accessed securely via _https_. Without this, both Docker and Minikube would require their configurations to be edited to allow insecure registries, ie. basic _http_.

#### Registry Pre-requisite
Whereas commands like `curl` allow Certificate-Authority (CA) certificates to be specified on the CLI, Docker does not. Therefore, if Docker encounters a secure registry, eg. when pushing, and does not recognise the CA it errors with _certificate signed by unknown authority_. So, Docker needs to be informed of the Minikube CA and this is done by:

  1. Create a new directory in Docker's certificate configuration and create a soft-link to the minkube CA certificate:
```
mkdir /etc/docker/certs.d/${REGHOST}:5000
ln -s ${HOME}/.minikube/ca.crt /etc/docker/certs.d/${REGHOST}:5000/
```
 
  2. Restart the Docker engine.

#### Registry Building and Deploying

>Note. If changing the IP address mapping to ${REGHOST}, be sure to update ${REGHOST}'s address **and** restart the `libvirtd` service.

 * Start the registry by executing:
```
 #
 # This command assumes the location of Minikube CA
 # from the following locations:
 # ${HOME}/.minikube/ca.key (CA key)
 # ${HOME}/.minikube/ca.crt (CA certificate)
 #
 # If a different CA is required then use
 # ${SYNDESIS} kube registry instead and specify when asked.
 #

 ${SYNDESIS} minikube registry
```

>Note. The Docker install should be configured to allow external network access to its containers or expose it ports when it is up and running.

 * To check the registry is successfully running, execute:
```
 #
 # This specifies the Minikube CA as the certificate to check the https
 # certificate against.
 #
 
 curl --cacert ~/.minikube/ca.crt https://${REGHOST}:5000/v2/_catalog

 # should return
 {"repositories":[]}
```

Building the Operator Image [&#8593;](#table-of-contents)
===========================
The operator is compiled locally using [go](https://golang.org/) and then optionally pushed to a [configured](#configuring-an-image-registry-) registry (depending on the option chosen).

The command for executing a build is:
```

#
# In all cases, the name:tag of the operator image will always match
# the tag in the registry/docker repository and in the deployment
# configuration generated by the operator
#

#
# For quay.io registry
# Necessary to override the operator-image to remove the syndesis prefix
# Image will be tagged as quay.io/${user}/syndesis-operator:latest
#

${SYNDESIS} build -m operator --image --docker \
  --registry quay.io/phantomjinx \
  --operator-image syndesis-operator

#
# For Minikube Docker (no registry necessary)
# Image will be tagged in the docker repository as the default of
# docker.io/syndesis/syndesis-operator:latest
#

${SYNDESIS} build -m operator --image --docker

#
# For Local Registry
# Changing the operator-image name & tag is not necessary,
# Image will be tagged as
# '${REGHOST}:5000/syndesis/syndesis-operator:latest'
#

${SYNDESIS} build -m operator --image --docker \
  --registry "${REGHOST}:5000"
```
Optional Switch | Usage
------------ | -------------
``--operator-tag <tag>`` | overrides the 'latest' tag

Environment Variable | Usage
------------ | -------------
``GOOSLIST=<os-list>`` | Speed up compilation by limiting operating systems to build against - space separated list of 1 or more of linux, windows, darwin

Once the build is complete, the command will copy the Syndesis Operator binary to the ${HOME}/.syndesis/bin directory.

Installing the Operator Image [&#8593;](#table-of-contents)
================

### Understanding Secrets [&#8593;](#table-of-contents)
In order to secure the Syndesis ingress path, [oauth2-proxy](https://github.com/oauth2-proxy/oauth2-proxy) is employed. This requires a two secrets to be created prior to the Syndesis install.
  
##### syndesis-oauth-credentials
Will make available to the oauth2-proxy, the credentials for the authentication provider to be used for authorising access to Syndesis. Initially, this has been simply configured with [github](github.com) as the provider in mind but has the facilities to use any of the providers detailed [here](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration). Set all configuration variables as data values in the secret (hence prefix with OAUTH2_PROXY), eg.
```
#
# Google Provider Secret
#
apiVersion: v1
kind: Secret
metadata:
  name: syndesis-oauth-credentials
  labels:
    app: syndesis
    syndesis.io/app: syndesis
    syndesis.io/type: infrastructure
stringData:
  OAUTH2_PROXY_PROVIDER: google
  OAUTH2_PROXY_CLIENT_ID: <client-id copied from google configuration>
  OAUTH2_PROXY_CLIENT_SECRET: <secret copied from google configuration>
```
Follow the instructions at [oauth2-proxy.github.io](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration#github-auth-provider) to configure access to preferred authentication provider. Make a note of the required configuration values and configure the secret appropriately.

  * If the secret requires only the provider, client_id & client_secret then the syndesis-oauth-credentials secret can be generated within the install command (type 'no' when asked to provide a custom secret file).
  * For more complex credentials, a file containing the whole secret should be made available to the install command (type 'yes' when asked to provide a custom secret file and enter the file path).

##### syndesis-oauth-comms [&#8593;](#table-of-contents)
Provides a signed (by the CA) certificate and key for the TLS _https_ connection of the oauth2-proxy. Executing via the `minikube install` command, the CA certificate and key should be picked up from the Minikube installation.

The certificate requires a hostname that represents the final external hostname for accessing Syndesis, eg. `https://${SYNDESIS_HOSTNAME}`. Once this hostname is chosen, a DNS record should be created that points it to the **Minikube IP** address (once directed to that IP, Minikube's internal routing should take care of finding the correct route to the Syndesis app). The configuration of DNS is a configuration left up to the user as this will depend on the user's own network setup.

### Executing The Install [&#8593;](#table-of-contents)

#### Preferred Command [&#8593;](#table-of-contents)
The `minikube` suite of commands have been included as convenience functions to wrap the component [tasks](#individual-task-commands). This is the preferred installation method for installing on Minikube. Only in cirumstances where troubleshooting or advanced requirements are necessary should component tasks commands be invoked.

Install Syndesis to Minikube using the following command:
```
${SYNDESIS} minikube install
```
  1. Attempts to start Minikube with defaults if not already running
  1. Creates, if necessary, a set of persistent volumes
  1. Creates the 'developer' user and 'syndesis' namespace
  1. Generates credential and comms secrets containing parameters for externally accessing Syndesis via an URL (requested at the appropriate point)
  1. Grants the appropriate permissions for installation
  1. Installs the Syndesis Operator and its components using a custom resource generated from the secrets and requested URL

#### Manual Commands [&#8593;](#table-of-contents)
This set of commands should be carried out in the event the [Preferred Command](#preferred-command) fails. They should be executed sequentially.

##### Install Pre-Requisites
Create the **developer** user:
```
${SYNDESIS} kube user developer -n syndesis
```

Minikube, unlike minishift or crc, does not pre-configure [persistent volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes). So execute the following command to create ten _hostPath_ volumes:
```
${SYNDESIS} minikube volumes
```

Executing the following command will create the oauth proxy secrets. It will also generate a default Kubernetes Custom Resource (CR) that should be used when completing the Syndesis install:
```
#
# The CR is created in ~/.syndesis/share/custom-resources
#

${SYNDESIS} kube secrets
```

##### Syndesis Install
Install the Syndesis custom resource definitions (CRDs) and grant the developer user the install permissions. This should be performed as a **cluster-admin**.
```
#
# Switch to the Minikube cluster-admin user
#

${SYNDESIS} kube user minikube -n syndesis

#
# Install the CRDs and grant permissions to the developer user
#

${SYNDESIS} install -p syndesis --setup --grant developer
```

Install the Syndesis operator and its components using the custom resource generated by the secrets commmand. The custom-resource is mandatory since it contains the properties `routeHostname`, `credentialsSecret` and `cryptoCommsSecret` which are themselves required for a Minikube installation.

```
#
# ${SYNDESIS_HOSTNAME}: hostname entered when creating the secrets
#

${SYNDESIS} install --dev \
  --custom-resource ~/.syndesis/share/custom-resources/${SYNDESIS_HOSTNAME}-cr.yml
```
Optional Switch | Usage
------------ | -------------
``--dev`` | ensures that images are always fetched from registries on every configuration changes (default is use cached images if already present)

>Note. If the `--custom-resource` option is NOT specified then an error will occur in the operator of the form `"The operator configuration requires a route hostname be defined"`. The `syndesis install` command does check for a custom-resource but if installing outside of this command, be aware a custom-resource is necessary.

Accessing Syndesis Application [&#8593;](#table-of-contents)
===============
Allowing a few minutes for all the Syndesis components to be deployed and initialised should result in an output similar to this:
```
kubectl get pods

NAME                                   READY   STATUS    RESTARTS   AGE
syndesis-db-c8d6bddc8-966tq            2/2     Running   0           4m
syndesis-meta-846d58cb7d-8s6rt         1/1     Running   0           4m
syndesis-oauthproxy-66c5f9f474-bbg4s   1/1     Running   0           4m
syndesis-operator-67b9f7dd5d-7cgpk     1/1     Running   0           4m
syndesis-prometheus-679dd686c9-8qp2j   1/1     Running   0           4m
syndesis-server-74945697f7-cw46t       1/1     Running   0           4m
syndesis-ui-665b8c88b8-vq2nk           1/1     Running   0           4m
```

Access Syndesis using the url `https://${SYNDESIS_HOSTNAME}`, where `${SYNDESIS_HOSTNAME}` is the hostname specified in the generated custom-resource.

Individual Task Commands [&#8593;](#table-of-contents)
============
### Creating a User [&#8593;](#table-of-contents)
```
${SYNDESIS} kube user <username> [-n <namespace>]

#
# eg. syndesis kube user developer -n syndesis
#     creates the user 'developer' & changes to 'syndesis' namespace
#
```

### Creating or Switching a Namespace [&#8593;](#table-of-contents)
```
${SYNDESIS} kube nm <namespace>

#
# eg. syndesis kube nm syndesis
#     creates the 'syndesis' namespace, if not already present
#     switches to the 'syndesis' namespace
#
```

### Re-creating a Namespace [&#8593;](#table-of-contents)
```
${SYNDESIS} kube nm -f <namespace>

#
# eg. syndesis kube nm -f syndesis
#     deletes the 'syndesis' namespace
#     creates the 'syndesis' namespace
#
```

### Displaying Cluster Name or URL [&#8593;](#table-of-contents)
```
${SYNDESIS} kube cluster

#
# returns name of cluster
# eg. minikube
#

${SYNDESIS} kube cluster -a

#
# returns url of cluster
# eg. https://192.168.39.17:8443
#
```

### List Configured Contexts [&#8593;](#table-of-contents)
```
${SYNDESIS} kube contexts

#
# returns configured contexts available in ~/.kube/config
# eg.
#   default/minikube/developer
#   default/minikube/minikube
#   minikube
#   syndesis/minikube/developer
#   syndesis/minikube/minikube
#
```

### Switch Context [&#8593;](#table-of-contents)
```
${SYNDESIS} kube context <context name>

#
# eg. syndesis kube context syndesis/minikube/developer
#     switches to the given context
#
```

### Generating Syndesis Secrets [&#8593;](#table-of-contents)
```
${SYNDESIS} kube secrets

#
# creates the syndesis-oauth-credentials & syndesis-oauth-comms secrets
#
```

### Starting a Local Docker Secure Registry [&#8593;](#table-of-contents)
```
#
# Will ask for a CA certificate and key for injecting into the registry
#
${SYNDESIS} kube registry

#
# Will assume the minikube CA certificate and key so inject those into the registry
#
${SYNDESIS} minikube registry

```

### Creating Minikube Volumes [&#8593;](#table-of-contents)
```
#
# Checks for available hostpath volumes and ensures 10 standard
# volumes are created 
#
${SYNDESIS} minikube volumes

```
