## Syndesis System Tests

This repository contains system tests for Syndesis.
These test are meant to be run against Openshift _(either within or pointing to an Openshift installation)_.

### Running the tests

To run the test locally:

    mvn clean test

### Running the tests against restricted environments

In restricted environments, creating a new project for the purpose of testing can be sometimes problematic, due to lack of permissions.
In such cases you can create a new project on manually and run the tests inside that project:

    oc new-project testing
    mvn clean test -Dnamespace.to.use=testing


### The testing framework

This project is using [Arquillian Cube](https://github.com/arquillian/arquillian-cube) as testing Framework.
Arquillian creates a namespace, installs Syndesis in it and once everything is ready, it starts the test suite.

More documentation at [Arquillian Cube with Kubernetes and Openshift](https://github.com/arquillian/arquillian-cube/blob/master/docs/kubernetes.adoc).
