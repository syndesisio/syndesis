# A Secure Docker Registry Image

This provides a Docker image of a Kubernetes registry that is secured, ie. uses a TLS certificate to mandate https encryption. This means it is no longer necessary to mark such a registry as unsecured in either the Docker or Kubernetes configuration.

Most instructions will require a certificate as a pre-requisite. However, this image allows the user to create a certificate that is signed by the Kubernetes Certificate Authority (CA).

**Note.**
This is not a substitute for applying a legitimate certificate to a registry but, for development purposes, provides a simple mechanism for achieving https suuport on a Docker registry and avoiding error messages (and effort) concerning insecure registeries.

## Pre-requisites
* The scripts assume the Kubernetes installation is minikube and check its up and running. However, the scripts are simple and could be easily modified, if required.
* Docker client

## Building

`minikube` is a Kubernetes implementation installed inside a VM. It has its own Docker daemon installed so it is possible to build the image directly inside the VM by executing the following:
> eval $(minikube docker-env)

Change into the `src` directory and execute the `build.sh` script with a hostname:
> ./build.sh -h host1.example.com

The hostname is inserted into the generated TLS certificate as a named DNS entry, ensuring that when Kubernetes and Docker access the registry, eg. `https://host1.example.com:5000/v2/_catalog`, the DNS name is validated successfully. The certificate also adds the alternative subject names of `localhost` and the minikube ip address.

The build script should successfully create an image, in its local repository.

## Executing

Run the script `sbin/registry-start.sh -h <hostname>` to fire up an instance of the image, substituting `<hostname>` for the hostname already used in the building of the image.

Assuming the image comes up correctly it should be possible to access the registry using `curl`, eg. `curl -k https://host1.example.com:5000/v2/_catalog`. 
