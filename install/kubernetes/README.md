# Installing Syndesis on Minikube (Development)

### Limitation & Caveat
An installation of Syndesis on Minikube is seen as an ongoing developmental effort, ie. not ready for production. There remains a significant missing piece, namely the runtime for generated integrations. This is being worked on.


### Variables
* **SYNDESIS**: the syndesis install binary, ie. ${syndesis directory}/tools/bin/syndesis
* **REGHOST** : a DNS name for accessing an image registry, eg. thor, loki. It should map to an IP address accessible by Minikube.

### Install Minikube
* Download and install Minikube by following the instructions [here](https://kubernetes.io/docs/tasks/tools/install-minikube);
* Execute the script:
```
#
# Use --driver to see the driver to use (kvm2 by default)
# Use --disk to allocate the size of storage (60GB by default)
# Use --memory to allocate the amount of memory (12GB by default)
# Use --cpus to allocate the number of CPUs (4 by default)
#
# Use --registry <file> to provide a file containing a list of
# registery urls that Minikube can access.
#
${SYNDESIS} minikube start
```
* Wait for Minikube to have started successfully.

### Enable Minikube addons
```
minikube addons enable dashboard # optional but useful for later development
minikube addons enable ingress
minikube addons enable ingress-dns
```

### Docker Registry
This requires a little explanation.

Any images on kubernetes platforms are fetched via an URL from a single location, a registry. This registry can be public, eg. `docker.hub.io`, or private, eg. `https://my.private.reg:5000`. The platform does not care except that it has been given access. For secure access, ie. _https_, a certificate is required that has been signed by the platform's own Certificate Authority (CA).

#### Using docker-env
The image is fetched from the external registry and placed in Minikube's own docker, tagged with the full name of the source registry. Therefore, one alternative is to build images directly into Minikube by first re-pointing the local docker daemon, using [docker-env](https://minikube.sigs.k8s.io/docs/commands/docker-env). This is regarded as the most efficient way of working if images are being rebuilt and debugged on a frequent basis.

#### Using an independent secure registry
Rather than pushing directly into Minikube's docker, images can be pushed into an interim registry, from which Minikube can access and fetch. Of course, it is possible to push the images to `hub.docker.io` but uploading images to the internet only to download them is a little wasteful. Minikube does include a registry-addon but it is currently insecure and has to be accessed using `localhost`. After some experimentation, this was dropped in favour of setting up a private secure registry in docker.

A hostname should be chosen and assigned to **REGHOST**. This is a hostname that points to an IP address, accessible by Minikube and will be used in registry-access commands, eg. `https://${REGHOST}:5000/v2/_catalog`. This hostname will be assigned as the CN value to a Minikube-CA-signed TLS certificate allowing the registry to be accessed securely via _https_. Without this, both Docker and Minikube would require their configurations to be edited to allow insecure registries, ie. basic _http_.

##### Registry Pre-requisite
Whereas commands like `curl` allow Certificate-Authority (CA) certificates to be specified on the CLI, Docker does not (grrr!). Therefore, if Docker encounters a secure registry, eg. when pushing, and does not recognise the CA it errors with _certificate signed by unknown authority_. So, Docker needs to be informed of the Minikube CA and this is done by:
* Create a new directory in Docker's certificate configuration and create a soft-link to the minkube CA certificate:
  ```
  mkdir /etc/docker/certs.d/${REGHOST}:5000
  ln -s ${HOME}/.minikube/ca.crt /etc/docker/certs.d/${REGHOST}:5000/
  ```
* Restart the Docker engine.

##### Registry Building and Deploying

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

>Note. The Docker install should be configured to allow external network access to its containers or expose it ports when its up and running.

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

### Building the Syndesis Operator
Prior to release or during development, the Syndesis Operator will have to be built rather than downloaded. This procedure will compile the operator and push it to the configured registry.

* If using the Minikube `docker-env` process, the `--registry` switch is unnecessary.

* Once the build is complete, the command will copy the Syndesis Operator binary to the ${HOME}/.syndesis directory.

```
${SYNDESIS} build -m operator -i --registry "${REGHOST}:5000" 
#
# Note. the port is 5000 as per the registry creation earlier.
#
``` 

### Syndesis Minikube Install Command
A convenience command has been included which is responsible for starting up Minikube, configuring Syndesis pre-requisites and installing the Syndesis Operator and components. This is the preferred installation method on Minikube. Only if working slightly differently or troubleshooting should the manual process below be attempted.

* Install Syndesis to Minikube using the following command:
```
${SYNDESIS} minikube install
#
# * Will attempt to start Minikube with defaults
#   if not already running
# * Will create, if necessary a set of persistent volumes
# * Will create the 'developer' user and 'syndesis' namespace
# * Will generate credential and comms secrets containing information
#   for externally accessing Syndesis via an URL (this will be requested
#   at the appropriate point
# * Will setup and grant the appropriate permissions for installation
# * Will install the Syndesis Operator and its components using a
#   custom resource generated from the secrets and requested URL
#
```

#### Detail on Secrets Generated During Install
In order to secure the Syndesis ingress path, [oauth2-proxy](https://github.com/oauth2-proxy/oauth2-proxy) is employed. This requires a couple of secrets to be created prior to the Syndesis install.
  
##### syndesis-oauth-credentials
  * Will make available to the oauth-proxy, the credentials for the authentication provider to be used for authorising access. Initially, this has been simply configured with [github](github.com) as the provider in mind but has the facilities to use any of the providers detailed [here](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration). Set all configuration variables as data values in the secret (hence prefix with OAUTH2_PROXY), eg.
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
  
  * Follow the instructions at [oauth2-proxy.github.io](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration#github-auth-provider) to configure access to preferred auth provider. Make a note of the required configuration values and configure the secret appropriately.
  * If the secret requires only the provider, client_id & client_secret then the syndesis-oauth-credentials secret can be generated using by the install command. Anything more complicated then a file should be made available to the install command instead.

##### syndesis-oauth-comms
  * Provides a signed (by the CA) certificate and key for the TLS _https_ connection of the oauth2-proxy. If using the `minikube install` command the CA certificate and key should be picked up from the Minikube installation.
  * The certificate requires a hostname that represents the final external hostname for accessing Syndesis, eg. `https://${SYNDESIS_HOSTNAME}`. Once this hostname is chosen, a DNS record should be created that points it to the **Minikube IP** address (once directed to that IP, Minikube's internal routing should take care of finding the correct route to the Syndesis app).


### Syndesis Manual Install Commands (for troubleshooting)

#### Install Pre-Requisites
* Create the **developer** user:
```
${SYNDESIS} kube user developer -n syndesis
```

* Minikube, unlike minishift or crc, does not pre-configure [persistent volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes). So execute the following command to create ten _hostPath_ volumes:
```
${SYNDESIS} minikube volumes
```

* Executing the following command will create the oauth proxy secrets. It will also generate a default Kubernetes Custom Resource (CR) that should be used when completing the Syndesis install:
```
${SYNDESIS} kube secrets
#
# The CR is created in ~/.syndesis/share/custom-resources
#
```

#### Syndesis Install
* Install the Syndesis custom resource definitions and grant the developer user the install permissions. This should be performed as a cluster-admin.
```
#
# Switch to the Minikube cluster-admin user
#
syndesis kube user minikube -n syndesis

#
# Install the CRDs and grant permissions to the developer user
#
syndesis install -p syndesis --setup --grant developer
```

* Install the Syndesis Operator and its components using the custom resource generated by the secrets commmand. The custom-resource is mandatory since it contains the properties `routeHostname`, `credentialsSecret` and `cryptoCommsSecret` which are required for a successful Minikube installation.
```
syndesis install --dev --custom-resource ~/.syndesis/share/custom-resources/${SYNDESIS_HOSTNAME}-cr.yml

# Where --dev ensure that images are always fetched from registries (default is use local if present)
# Where ${SYNDESIS_HOSTNAME} is the hostname entered while creating the secrets above
```
* If the `--custom-resource` option is not specified then an error will occur in the operator of the form `"The operator configuration requires a route hostname be defined`. The `syndesis install` command does check for a custom-resource but if installing outside of this command, be aware a custom-resource is necessary.

### Accessing Syndesis App
Allow a few minutes for all the Syndesis components to be deployed then access Syndesis using the url `https://${SYNDESIS_HOSTNAME}`.
