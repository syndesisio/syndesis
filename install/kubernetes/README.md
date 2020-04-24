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
This requires a little explanation. When the operator image is built, it then needs to be pushed to a registry that Minikube is able to access. Of course, it's possible to push the images to `hub.docker.io` but uploading images to the internet only to bring them back down is a bit of a waste. Unlike Openshift, Minikube does not have an out-of-the-box turn-key registry to build into (it does have a registry addon but doesn't seem to work correctly!). So, (for the moment) this provides a private secure registry for Minikube access.


A hostname should be chosen and assigned to **REGHOST**. This is a hostname that points to an IP address, accessible by Minikube and will be used in registry-access commands, eg. `https://${REGHOST}:5000/v2/_catalog`. This hostname will be assigned as the CN value to a signed TLS certificate allowing the registry to be accessed via _https_. Without this, both Docker and Minikube would require their configurations editing to allow insecure registries, ie. basic _http_.

##### Registry Pre-requisite
Whereas commands like `curl` allow Certificate-Authority (CA) certificates to be specified on the CLI, Docker does not (grrr!). Therefore, if Docker encounters a secure registry, eg. when pushing, and does not recognise the CA it errors with _certificate signed by unknown authority_. So, Docker needs to be informed of the Minikube CA and this is done by:
* Create a new directory in Docker's certificate configuration and create a soft-link to the minkube CA certificate:
  ```
  mkdir /etc/docker/certs.d/${REGHOST}:5000
  ln -s ${HOME}/.minikube/ca.crt /etc/docker/certs.d/${REGHOST}:5000/
  ```
* Restart the Docker engine.

##### Registry Building and Deploying

>Note. If changing the IP address mapping to ${REGHOST}, be sure to update ${REGHOST}'s address then restart the `libvirtd` service.

 * Start the registry by executing:
 ```
 ${SYNDESIS} kube registry
 ```

>Note. The Docker install should be configured to allow external network access to its containers.

 * To check the registry is successfully running, execute:
 ```
 curl --cacert ~/.minikube/ca.crt https://${REGHOST}:5000/v2/_catalog

 # should return
 {"repositories":[]}
 ```

### Syndesis Pre-Requisites
* Create the **developer** user:
```
${SYNDESIS} kube user developer -n syndesis
```

* Minikube, unlike minishift or crc, does not pre-configure [persistent volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes). So execute the following command to create ten _hostPath_ volumes:
```
${SYNDESIS} minikube volumes
```

* In order to secure the Syndesis ingress path, [oauth2-proxy](https://github.com/oauth2-proxy/oauth2-proxy) is employed. This requires a couple of secrets to be created prior to the Syndesis install.
  1. **syndesis-oauth-credentials**:
    * Will make available to the oauth-proxy, the credentials for the authentication provider to be used for authorising access. Initially, this has been simply configured with [github](github.com) as the provider in mind.
    * Follow the instructions at [oauth2-proxy.github.io](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration#github-auth-provider) to configure a github auth provider. Make a note of _Provider_ (ie. github), _Client Id_ and _Client Secret_ as these will be requested later.
  2. **syndesis-oauth-comms**:
    * Provides a signed (by the CA) certificate and key for the TLS _https_ connection of the oauth2-proxy.
    * The certificate will require a hostname that represents the final external hostname for accessing Syndesis, eg. `https://${SYNDESIS_HOSTNAME}`. Once this hostname is chosen, a DNS record should be created that points it to the **Minikube IP** address (once directed to that IP, Minikube's internal routing should take care of finding the correct app).

* Executing the following will create the secrets. It will also generate a default Kubernetes Custom Resource (CR) that should be used when completing the Syndesis install:
```
#
# The CR is created in ~/.syndesis/share/custom-resources
#
${SYNDESIS} kube secrets
```

### Syndesis Operator Building

* Build the Syndesis operator image, push it to the [docker registry](#docker-registry). Once complete this will also copy the Syndesis Operator binary to the ${HOME}/.syndesis directory:

```
${SYNDESIS} build -m operator -i --registry "${REGHOST}"
``` 

### Syndesis Install
```
syndesis install -p syndesis --setup --grant developer
```

```
syndesis install --custom-resource ~/.syndesis/share/custom-resources/${SYNDESIS_HOSTNAME}-cr.yml

# Where ${SYNDESIS_HOSTNAME} is the hostname entered while creating the secrets above
```


* Install the Syndesis CR generated by the `syn-create-secrets` script by executing:
```
syn-exec-op -c ${K8}/cr/kube-cr.yml
```

* Allow a few minutes for all the Syndesis components to be deployed then access Syndesis using the url `https://<syndesis-hostname>` (where \<syndesis-hostname> was the hostname specified to `syn-create-secrets`).
