# Red Hat iPaaS API

[![CircleCI](https://img.shields.io/circleci/project/github/redhat-ipaas/ipaas-rest.svg)](https://circleci.com/gh/redhat-ipaas/ipaas-rest)
[![Maven Central](https://img.shields.io/maven-central/v/com.redhat.ipaas/ipaas-rest.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22redhat-ipaas%22%20AND%20a%3A%22ipaas-rest%22)
[![Dependency Status](https://dependencyci.com/github/redhat-ipaas/ipaas-rest/badge)](https://dependencyci.com/github/redhat-ipaas/ipaas-rest)

- [Building](#building)
- [Running](#run-in-development-mode)
- [Deploying](#deploying-to-kubernetes)
- [Endpoints](#endpoints)

### Swagger
[![Swagger](http://dgrechka.net/swagger_validator_content_type_proxy.php?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-rest/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)](https://online.swagger.io/validator/debug?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-rest/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)

# Building

    mvn clean install

# Run in development mode

    cd runtime
    mvn clean package wildfly-swarm:run

# Deploying to Kubernetes

    oc login <KUBERNETES_MASTER>
    cd runtime
    mvn clean package fabric8:build fabric8:deploy fabric8:start

# Endpoints

    REST service: http://localhost:8080/v1/
    Swagger doc:  http://localhost:8080/v1/swagger.json
    SwaggerUI:    http://localhost:8080/swagger-ui/

There is a [Demo endpoint](http://runtime-kurt.b6ff.rh-idev.openshiftapps.com/swagger-ui/) on OpenShift dedicated.
This demo endpoint has some preloaded data and can be used for testing and demoing purposes.

# Authentication

Authentication is provided by KeyCloak. At the moment authentication is turned off. To enable it uncomment 

....archive.as(Secured.class).
....protect( "/components/*").
....withMethod( "GET" ).
....withRole( "citizen_developer" );

in the Main.java to enable protection for doing a GET on 'components'. 

Currently the KeyCloak server is running on this very same swarm instance. The runtime is configured to use the 'ipaas-auth' realm via the settings in the src/main/resources/keycloak.json file. When the server is first started, at 'http://localhost:8080/auth', the user is prompted to create an admin user for the 'master' realm. Once the admin account is created and the user is logged in, the ipaas-auth realm should be created using the 'Add realm' button that shows up when hovering over the 'Master' realm in top left of the admin UI. Click this button and select to import the ipaas-realm.json file from your filesystem.

At this point your browser will redirect to the login page when navigating to 'http://localhost:8080/v1/components'. You can login with nyc/citizen. A token will be set in your browser but it is also possible to explicitly obtain this token using

....curl --data "grant_type=password&client_id=ipaas-auth-service&username=nyc&password=citizen" http://localhost:8080/auth/realms/ipaas-auth/protocol/openid-connect/token

See also http://blog.keycloak.org/2015/10/getting-started-with-keycloak-securing.html


