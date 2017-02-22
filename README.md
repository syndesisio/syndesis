# Red Hat iPaaS API

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/daf25eee770345c9b72a2b8aecb90182)](https://www.codacy.com/app/jimmidyson/ipaas-rest?utm_source=github.com&utm_medium=referral&utm_content=redhat-ipaas/ipaas-rest&utm_campaign=badger)
[![CircleCI](https://circleci.com/gh/redhat-ipaas/ipaas-rest.png)](https://circleci.com/gh/redhat-ipaas/ipaas-rest)
[![AppVeyor](https://ci.appveyor.com/api/projects/status/v6ycvs9nw6o2t821/branch/master?svg=true)](https://ci.appveyor.com/project/jimmidyson/ipaas-rest/)
[![Maven Central](https://img.shields.io/maven-central/v/com.redhat.ipaas/ipaas-rest.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22redhat-ipaas%22%20AND%20a%3A%22ipaas-rest%22)
[![Dependency Status](https://dependencyci.com/github/redhat-ipaas/ipaas-rest/badge)](https://dependencyci.com/github/redhat-ipaas/ipaas-rest)

- [Building](#building)
- [Running](#run-in-development-mode)
- [Deploying](#deploying-to-kubernetes)
- [Endpoints](#endpoints)
- [Authentication](#authentication)

### Swagger
[![Swagger](http://dgrechka.net/swagger_validator_content_type_proxy.php?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-rest/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)](https://online.swagger.io/validator/debug?url=https://circleci.com/api/v1/project/redhat-ipaas/ipaas-rest/latest/artifacts/0/$CIRCLE_ARTIFACTS/swagger.json)

# Building

    mvn clean install

# Run in development mode

Linux:

    ./start-with-keycloak.sh
    
Windows:

    start-with-keycloak

# Deploying to Kubernetes

    oc login <KUBERNETES_MASTER>
    cd runtime
    mvn clean package fabric8:build fabric8:deploy fabric8:start

# Endpoints

* REST service: [http://localhost:8080/api/v1/](http://localhost:8080/api/v1/)
* Swagger doc:  [http://localhost:8080/api/v1/swagger.json](http://localhost:8080/api/v1/swagger.json)

There is a [staging URL](https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/api/v1/) on OpenShift dedicated that you can test with, along with the [Swagger JSON](https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json) [![Swagger UI](http://petstore.swagger.io/images/logo_small.png)](http://petstore.swagger.io/?url=https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json).

This demo endpoint has some preloaded data and can be used for testing and demoing purposes.

# Authentication

The REST API is protected via an OAuth 2.0 Bearer Token. The REST API server is a resource server as defined in
[The OAuth 2.0 Authorization Framework: Bearer Token Usage](https://tools.ietf.org/html/rfc6750). This requires all requests
include a valid access token in the `Authorization` header of the request (there are other methods but they are discouraged).
The header should look like:

    Authorization: Bearer MDQyODExLCJpc3MiOi...

The current implementation uses [Keycloak](http://keycloak.org/) as an OAuth Authorization Server. Keycloak provides the capability
to broker multiple identity providers and this allows the iPaaS to be independent of identity providers.

## Getting an access token locally

If you want to try out the REST API manually, try the following steps (note that this requires installation of the amazing [`jq`]()):

```bash
$ ./start-with-keycloak.sh

# In another terminal... get a token from keycloak authenticating with `user`/`password`
$ TOKEN=$(curl \                                                                
    -d "client_id=admin-cli" \ 
    -d "username=user" \
    -d "password=password" \
    -d "grant_type=password" \
    "http://localhost:8282/auth/realms/ipaas-test/protocol/openid-connect/token" | jq -r .access_token)
    
$ curl http://localhost:8080/api/v1/components -H "Authorization: Bearer $TOKEN"

# Validate the REST API requires the valid token - should return a 401
$ curl http://localhost:8080/api/v1/components
```
