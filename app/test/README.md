# Syndesis Testing

This repository contains integrated e2e tests for Syndesis.

## Integration Tests

These tests start Syndesis integration runtimes in [Docker](https://www.docker.com/) 
(using [Testcontainers](https://www.testcontainers.org/)) in order to exchange messages with the running integration. 

The integration outcome gets consumed and verified by simulated 3rd party services and/or within the database.

Read the [documentation](integration-test) for details.

## System Tests

These system tests are meant to be run against Openshift _(either within or pointing to an Openshift installation)_ and perform
a full installation of Syndesis in that environment.

Read the [documentation](system-test) for details.
