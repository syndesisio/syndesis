## Funktion

**Funktion** implements a simple event driven lambda style programming model on top of [Kubernetes](http://kubernetes.io).

A 'funktion' is a function in some programming language bound to a trigger deployed into Kubernetes!

### Using funktion

You write a simple function in any programming language [like this](https://github.com/fabric8io/funktion/blob/master/funktion-runtime/src/test/java/io/fabric8/funktion/sample/Main.java#L25-L27).

Then you associate it with an event trigger, such as a HTTP URL or email address to listen on, a message queue name or database table etc. Hundreds of different trigger endpoints are supported including most network protocols, transports, databases, messaging systems, social networks, cloud services and SaaS offerings.

You add the event trigger URL to the [funktion.yml](funktion-runtime/funktion.yml) file and then kick off the build and your funktion will be deployed to your kubernetes cluster!

![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)

<p align="center">
  <a href="http://fabric8.io/">
  	<img src="https://raw.githubusercontent.com/fabric8io/fabric8/master/docs/images/cover/cover_small.png" alt="fabric8 logo"/>
  </a>
</p>

### Contributing to the project

We love [contributions](http://fabric8.io/contributing/index.html) and you are very welcome to help.

### License

This project is [Apache Licensed](license.txt)
