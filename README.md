### IPaaS System Tests

This repository contains system tests for Redhat IPaaS.
These test are meant to be run against Openshift _(either within or pointing to an Openshift installation)_.

#### Running the tests

To run the test locally:

    mvn clean test


#### The testing framework

This project is using [Arquillian Cube](https://github.com/arquillian/arquillian-cube) as testing Framework.
Arquillian creates a namespace, installs IPaaS in it and once everything is ready, it starts the test suite.

More documentation at [Arquillian Cube with Kubernetes and Openshift](https://github.com/arquillian/arquillian-cube/blob/master/docs/kubernetes.adoc).

