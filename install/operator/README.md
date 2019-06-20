# Syndesis Infrastructure Operator

An operator for installing and updating [Syndesis](https://github.com/syndesisio/syndesis).

# States

The operator follows the following state diagram:

![Syndesis Operator State Diagram](docs/syndesis-operator-states.png "Syndesis Operator State Diagram")

A summary of the states:

* **NotInstalled**: Syndesis resources that cannot be installed (e.g. two resources on the same namespace) are put into this state
* **Installing**: Syndesis installation started, resources are being created
* **Starting**: Creation of resources is completed, waiting for all deployments to be ready
* **StartupFailed**: Some deployments could not be started after all possible attempts. The state moves away from here if something is changed manually
* **Installed**: Everything is installed and the application is ready to be used
* **Upgrading**: The operator has detected that there's a new version and it has already started the upgrade process
* **UpgradeFailureBackoff**: A problem has occurred during the upgrade. Everything should have been restored and the upgrade process will be retried with a exponential delay (up to a maximum number of times)
* **UpgradeFailed**: After the maximum amount of failed upgrades, the upgrade will not be tried anymore. The CR needs a manual action to move away from here
* **UpgradingLegacy**: The CR can go into this state only if the operator has detected that there's a legacy installation of Syndesis in the watched namespace and there's no Syndesis resource that can own it in the same namespace. The operator then creates a Syndesis resource using a configuration inferred from the legacy environment variables


## Building

Just run:

````bash
$ ./build.sh
````

By default it tries to build the operator executable and container image using
the following tools in this order:

 1. go and oc
 2. go and docker
 3. docker and oc
 4. just docker


So at a minimum you should have one of those of tools installed.
   
    usage: ./build.sh [options]
    
    where options are:
      --help                             display this help messages
      --operator-build <auto|docker|go>  how to build the operator executable (default: auto)
      --image-build <auto|docker|s2i>    how to build the image (default: auto)
      --image-name <name>                docker image name (default: syndesis/syndesis-operator)
      --s2i-stream-name <name>           s2i image stream name (default: syndesis-operator)

