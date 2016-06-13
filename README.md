## <img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" width="24" height="24"/>&nbsp; Funktion

**Funktion** is an open source event driven lambda style programming model on top of [Kubernetes](http://kubernetes.io).

A _funktion_ is a regular function in any programming language bound to a _trigger_ deployed into Kubernetes. Then Kubernetes takes care of the rest (scaling, high availability, load balancing, logging and metrics etc).

Funktion supports hundreds of different [trigger endpoint URLs](http://camel.apache.org/components.html) including most network protocols, transports, databases, messaging systems, social networks, cloud services and SaaS offerings.

In a sense funktion is a [serverless](https://www.quora.com/What-is-Serverless-Computing) approach to event driven microservices as you focus on just writing _funktions_ and Kubernetes takes care of the rest. Its not that there's no servers; its more that you as the funktion developer don't have to worry about managing them.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.fabric8.funktion/funktion-runtime/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.fabric8.funktion/funktion-runtime/) ![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)

<p align="center">
  <a href="http://fabric8.io/">
  	<img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" alt="funktion logo" width="200" height="200"/>
  </a>
</p>


### Using funktion

* You write a simple function in any programming language [like this](https://github.com/fabric8io/funktion/blob/master/funktion-runtime/src/test/java/io/fabric8/funktion/sample/Main.java#L25-L27).

* Create a [funktion.yml](funktion-runtime/funktion.yml) file and associate your function with an [event trigger endpoint URL](http://camel.apache.org/components.html) such as a HTTP URL or email address to listen on, a message queue name or database table etc.

* Kick off the build (usually a [Jenkins CI / CD pipeline](http://fabric8.io/guide/cdelivery.html) and your funktion will be deployed to your kubernetes cluster!

* Win! :)


### Examples

Check out the following example projects:

* [funktion-java-example](https://github.com/fabric8-quickstarts/funktion-java-example) is an example using a Java funktion triggered by HTTP
* [funktion-groovy-example](https://github.com/fabric8-quickstarts/funktion-groovy-example) is an example using a [Groovy](http://www.groovy-lang.org/) funktion triggered by HTTP
* [funktion-kotlin-example](https://github.com/fabric8-quickstarts/funktion-kotlin-example) is an example using a [Kotlin](https://kotlinlang.org/) funktion triggered by HTTP
* [funktion-nodejs-example](https://github.com/fabric8-quickstarts/funktion-nodejs-example) is an example using a [NodeJS](https://nodejs.org/en/) funktion triggered by HTTP


### How it works

When you implement your **Funktion** using a JVM based language like Java, Groovy, Kotlin or Scala then your function is packaged up into a [Spring Boot](http://projects.spring.io/spring-boot/) application using [Apache Camel](http://camel.apache.org/) to implement the trigger via the various [endpoint URLs](http://camel.apache.org/components.html).

When using non-JVM based languages to implement your **Funktion** then the [Spring Boot](http://projects.spring.io/spring-boot/) and Camel based trigger processor is embedded into your [Kubernetes Pod](http://kubernetes.io/docs/user-guide/pods/) via a [sidecar container](http://blog.kubernetes.io/2015/06/the-distributed-system-toolkit-patterns.html) which then invokes your funktion; usually via a local REST call.

The creation of the docker images and generation of the kubernetes manifests is all done by the [fabric8-maven-plugin](https://github.com/fabric8io/fabric8-maven-plugin).

Underneath the covers a [Kubernetes Deployment](http://kubernetes.io/docs/user-guide/deployments/) is automatically created for you Funktion (or on OpenShift a [DeploymentConfig](https://docs.openshift.com/enterprise/3.0/dev_guide/deployments.html) is used) which takes care of scaling your funkion and performing [rolling updates](http://kubernetes.io/docs/user-guide/rolling-updates/) as you edit your code.

To simplify building, testing, staging, approving, releasing and managing code changes to your funkion we recommend you use the [Fabric8 Microservices Platform](http://fabric8.io/) with its baked in [Continuous Deployment](http://fabric8.io/guide/cdelivery.html) based on [Jenkins Pipelines](https://jenkins.io/solutions/pipeline/) together with integrated [Developer Console](http://fabric8.io/guide/console.html), [Management](http://fabric8.io/guide/management.html) (centralised logging, metrics, alerts), [ChatOps](http://fabric8.io/guide/chat.html) and [Chaos Monkey](http://fabric8.io/guide/chaosMonkey.html).

When using the [Fabric8 Microservices Platform](http://fabric8.io/) you can create a new funktion in a few clicks; then the platform takes care of building, testing, staging and approving your releases, rolling upgrades, management and monitoring; you just focus on changing your funktion source code ;)

### Video

Coming soon!!! :)

### Contributing to the project

We love [contributions](http://fabric8.io/contributing/index.html) and you are very welcome to help.

### License

This project is [Apache Licensed](license.txt)
