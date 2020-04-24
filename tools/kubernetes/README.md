# Installing Syndesis on Minikube (Development)

### Limitation & Caveat
An installation of Syndesis on minikube is seen as an ongoing developmental effort, ie. not ready for production. There remains a significant missing piece, namely the runtime for generated integrations. This is being worked on.


### Variables
* **K8** : the kubernetes directory under the codebase, ie. syndesis/tools/kubernetes
* **MKHOST** : a DNS name for accessing minikube, eg. minikube, thor, loki, so should map to the minikube IP address.

### Install minikube
* Download and install minikube by following the instructions [here](https://kubernetes.io/docs/tasks/tools/install-minikube);
* Edit the `${K8}/minikube/minikube-start.sh` script according to computer constraints, eg. 12GB memory may be too much!
* Execute the script:
```
${K8}/minikube/minikube-start.sh
```
* Wait for minikube to have started successfully.

### Enable minikube addons
```
minikube addons enable dashboard # optional but useful for later development
minikube addons enable ingress
minikube addons enable ingress-dns
```

### Docker Registry
This requires a little explanation. The operator image needs to be built locally then pushed to a registry that minikube is able to access. Of course, it's possible to push the images to hub.docker.io but seemingly uploading images to the internet only to bring them back down seems a bit of a waste. Unlike Openshift, minikube does not have an out-of-the-box turn-key registry to build into (it does have a registry addon but doesn't seem to work correctly!). So, this provides a private secure registry for minikube access.


A hostname is going to be chosen and assigned to **MKHOST**. This is a hostname that points to the minikube IP address and will be used in registry access commands, eg. `https://${MKHOST}:5000/v2/_catalog`. This hostname will be assigned as the CN value to a signed TLS certificate allowing the registry to be accessed via _https_. Without this, both docker and minikube would require their configurations editing to allow insecure registries, ie. basic _http_.

##### Registry Pre-requisite
Whereas commands like `curl` allow certificate authority (CA) certificates to be specified on the CLI, Docker does not (grrr!). Therefore, if Docker encounters a secure registry, eg. when pushing, and doesn't recognise the CA it errors with _certificate signed by unknown authority_. So, Docker needs to be informed of the minikube CA and this is done by:
* Create a new directory in Docker's certificate configuration and create a soft-link to the minkube CA certificate:
  ```
  mkdir /etc/docker/certs.d/${MKHOST}:5000
  ln -s ${HOME}/.minikube/ca.crt /etc/docker/certs.d/${MKHOST}:5000/
  ```
* Restart the Docker engine.

##### Registry Building and Deploying

>Note. If repeating this procedure after resetting minikube then the latter's IP address will have changed so any DNS records will need updating. Be sure to update ${MKHOST}'s address then restart the `libvirtd` service.

 * Build the docker image by executing:
 ```
 ${K8}/registry/build -h ${MKHOST}
 ```
 * Start the registry by executing:
 ```
 ${K8}/registry/sbin/registry-start.sh -h ${MKHOST}
 ```
 
>Both of these scripts make minikube's docker repository local effectively building and running the instance inside minikube's VM)

 * To check the registry is successfully running, execute:
 ```
 curl --cacert ~/.minikube/ca.crt https://${MKHOST}:5000/v2/_catalog
 
 # should return
 {"repositories":[]}
 ```
 
### Syndesis Pre-Requisites
* Create the **developer** user:
```
${K8}/bin/kube-user -u developer -n syndesis
```

* Minikube, unlike minishift or crc, does not have any [persistent volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes) pre-configured. So execute the following script to create the syndesis-db and 9 other _hostPath_ volumes:
```
${K8}/bin/syn-create-persistent-volumes
```

* In order to secure the Syndesis ingress path, [oauth2-proxy](https://github.com/oauth2-proxy/oauth2-proxy) is employed. This requires a couple of secrets to be created prior to the Syndesis install.
  1. **syndesis-oauth-credentials**: 
    * Will make available to the oauth-proxy, the credentials for the authentication provider to be used for authorising access. Initially, this has been simply configured with [github](github.com) as the provider in mind.
    * Follow the instructions at [oauth2-proxy.github.io](https://oauth2-proxy.github.io/oauth2-proxy/auth-configuration#github-auth-provider) to configure a github auth provider. Make a note of _Provider_ (ie. github), _Client Id_ and _Client Secret_ as these will be requested later.
  2. **syndesis-oauth-comms**:
    * Provides a signed (by the CA) certificate and key for the TLS _https_ connection of the oauth2-proxy.
    * The certificate will require a hostname that represents the final external hostname for accessing Syndesis, eg. `https://<syndesis-hostname>`. Once this hostname is chosen, a DNS record should be created that points it to the minikube IP address.

* Executing the following will create the secrets. It will also generate a default Kubernetes Custom Resource (CR) that should be used when completing the Syndesis install:
```
${K8}/bin/syn-create-secrets
```

### Syndesis Operator Building

* Build the Syndesis operator image, push it to the [docker registry](#docker-registry) and then deploy it to minikube:
```
kube-syn-build-operator -h ${MKHOST}

# The script will check to ensure the operator is running.
```

### Syndesis Install

* Install the Syndesis CR generated by the `syn-create-secrets` script by executing:
```
syn-exec-op -c ${K8}/cr/kube-cr.yml
```

* Allow a few minutes for all the Syndesis components to be deployed then access Syndesis using the url `https://<syndesis-hostname>` (where \<syndesis-hostname> was the hostname specified to `syn-create-secrets`).




