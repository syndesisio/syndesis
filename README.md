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

### Contributing to the project

We love [contributions](http://fabric8.io/contributing/index.html) and you are very welcome to help.

### License

This project is [Apache Licensed](license.txt)
