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


### Getting started

You can just fork one of the above examples and use command line tools to build and deploy it to a Kubernetes or OpenShift cluster.

However to make it easier to create, build, test, stage, approve, release, manage and iterate on your funktion code from inside your browser we recommend you use the [Fabric8 Microservices Platform](http://fabric8.io/) with its baked in [Continuous Deployment](http://fabric8.io/guide/cdelivery.html) based on [Jenkins Pipelines](https://jenkins.io/solutions/pipeline/) together with integrated [Developer Console](http://fabric8.io/guide/console.html), [Management](http://fabric8.io/guide/management.html) (centralised logging, metrics, alerts), [ChatOps](http://fabric8.io/guide/chat.html) and [Chaos Monkey](http://fabric8.io/guide/chaosMonkey.html).

When using the [Fabric8 Microservices Platform](http://fabric8.io/) you can create a new funktion in a few clicks from the `Create Application` button; then the platform takes care of building, testing, staging and approving your releases, rolling upgrades, management and monitoring; you just use your browser via the [Developer Console](http://fabric8.io/guide/console.html) to create, edit or test your funktion while funktion, Jenkins and Kubernetes take care of building, packaging, deploying, testing and releasing your project.

#### Using the Fabric8 Microservices Platform

First you will need to ensure you are running the `CD Pipeline` app in fabric8. Here's instructions:

* follow one of the [fabric8 getting started guides](http://fabric8.io/guide/getStarted/index.html) to get the [fabric8 microservices platform](http://fabric8.io/) up and running on a Kubernetes or OpenShift cluster
* open the [Developer Console](http://fabric8.io/guide/console.html)
* click the `Create Team` button to create a team which is a place for you and your team to create funktions.
* select the `Existing Namespace` from the `Create Using` form entry along with choosing the kubernetes namespace where you installed fabric8 (e.g. `default`).
* the UI will prompt you to ensure the `CD Pipeline` app from fabric8 is running to get the full [Continuous Deployment capability](http://fabric8.io/guide/cdelivery.html).
* There may now be a delay of up to an hour depending on your internet connection while the docker images get pulled down to your Kubernetes cluster and things startup. Please be patient.
* You can watch progress on the command line via: `kubectl get pods -w` or on OpenShift:`oc get pods -w`
* Eventually you wil be presented with a `Create Application` button on the `Team` page
* Note that we are in the process of optimising the above UX so its much easier and more straightforward! :)


#### Create and use your funktion

* from inside your `Team` page click `Create Application` button then you will be presented with a number of different kinds of microservice to create
* select the `Funktion` icon and type in the name of your microservice and hit `Next`
* select the kind of funktion you wish to create (Java, Groovy, Kotlin, NodeJS etc) then hit `Next`
* you will now be prompted to choose one of the default CD Pipelines to use. For your first funktion we recommend `CanaryReleaseAndStage`
* selecting `Copy pipeline to project` is kinda handy if you want to edit your `Jenkinsfile` from your source code later on
* click `Next` then your app should be built and deployed. Please be patient first time you build a funktion as its going to be downloading a few docker images to do the build and runtime. You're second build should be much faster!
* once the build is complete you should see on the `App Dashboard` page the build pipeline run, the running pods for your funktion in each environment for your CD Pipeline and a link so you can easily navigate to the environment or ReplicaSet/ReplicationController/Pods in kubernetes
* in the screenshot below you can see we're running version `1.0.1` of the app `groovyfunktion` which currently has `1` running pod (those are all clickable links to view the ReplicationController or pods)
* for HTTP based funktions you can invoke the funktion via the open icon in the `Staging` environment (the icon to the right of the green `1` button next to `groovyfunction-1: 1.0.1`)

![Funktion dashboard](https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/funktion-dashboard.png)


### Video demo

Coming soon!!! :)


### How it works

When you implement your **Funktion** using a JVM based language like Java, Groovy, Kotlin or Scala then your function is packaged up into a [Spring Boot](http://projects.spring.io/spring-boot/) application using [Apache Camel](http://camel.apache.org/) to implement the trigger via the various [endpoint URLs](http://camel.apache.org/components.html).

When using non-JVM based languages to implement your **Funktion** then the [Spring Boot](http://projects.spring.io/spring-boot/) and Camel based trigger processor is embedded into your [Kubernetes Pod](http://kubernetes.io/docs/user-guide/pods/) via a [sidecar container](http://blog.kubernetes.io/2015/06/the-distributed-system-toolkit-patterns.html) which then invokes your funktion; usually via a local REST call [at least for now](https://github.com/fabric8io/funktion/issues/11. Note that the use of sidecar is our current implementation strategy; going forward we'll support various options such as separating the trigger containers from the funktion containers for independent scaling for better resource utilisation.

We've focussed `funktion` on being some simple declarative metadata to describe triggers via URLs and a simple programming model which is the only thing funktion developers should focus on; leaving the implementation free to use different approaches for optimal resource usage.

The creation of the docker images and generation of the kubernetes manifests is all done by the [fabric8-maven-plugin](https://github.com/fabric8io/fabric8-maven-plugin) which can work with pure docker on Kubernetes or reuse OpenShift's binary source to image builds.

Underneath the covers a [Kubernetes Deployment](http://kubernetes.io/docs/user-guide/deployments/) is automatically created for your Funktion (or on OpenShift a [DeploymentConfig](https://docs.openshift.com/enterprise/3.0/dev_guide/deployments.html) is used) which takes care of scaling your funktion and performing [rolling updates](http://kubernetes.io/docs/user-guide/rolling-updates/) as you edit your code.

### Contributing to the project

We love [contributions](http://fabric8.io/contributing/index.html) and you are very welcome to help.

### License

This project is [Apache Licensed](license.txt)
