## <img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" width="24" height="24"/>&nbsp; Funktion

**Funktion** is an open source event driven lambda style programming model on top of [Kubernetes](http://kubernetes.io).

Funktion supports hundreds of different [trigger endpoint URLs](http://camel.apache.org/components.html) including most network protocols, transports, databases, messaging systems, social networks, cloud services and SaaS offerings.

In a sense funktion is a [serverless](https://www.quora.com/What-is-Serverless-Computing) approach to event driven microservices as you focus on just writing _funktions_ and Kubernetes takes care of the rest. Its not that there's no servers; its more that you as the funktion developer don't have to worry about managing them.

A _funktion_ is a regular function in any programming language which is deployed into Kubernetes and is _subscribed_ to events which then invoke the underlying function. Kubernetes takes care of the rest (scaling, high availability, load balancing, logging and metrics etc).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.fabric8.funktion/funktion-runtime/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/io.fabric8.funktion/funktion-runtime/) ![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)

<p align="center">
  <a href="http://fabric8.io/">
  	<img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" alt="funktion logo" width="200" height="200"/>
  </a>
</p>


### Getting Started

Please [Install Funktion](https://funktion.fabric8.io/docs/#install) then follow the [Getting Started Guide](https://funktion.fabric8.io/docs/#get-started) 

### License

This project is [Apache Licensed](license.txt)
