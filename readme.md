# Red Hat iPaaS API

[![CircleCI](https://img.shields.io/circleci/project/github/redhat-ipaas/ipaas-api-java.svg)](https://circleci.com/gh/redhat-ipaas/ipaas-api-java)
[![Maven Central](https://img.shields.io/maven-central/v/com.redhat.ipaas/ipaas-api-java.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22redhat-ipaas%22%20AND%20a%3A%22ipaas-api-java%22)

- [Building](#building)
- [Running](#run-in-development-mode)
- [Deploying](#deploying-to-kubernetes) 
- [Endpoints](#endpoints)

### Swagger
[![Swagger](https://online.swagger.io/validator?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-api-java/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)](https://online.swagger.io/validator/debug?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-api-java/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)

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
