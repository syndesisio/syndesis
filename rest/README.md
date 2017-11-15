# Syndesis REST API

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/daf25eee770345c9b72a2b8aecb90182)](https://www.codacy.com/app/syndesisio/syndesis-rest)
[![CircleCI](https://circleci.com/gh/syndesisio/syndesis-rest.png)](https://circleci.com/gh/syndesisio/syndesis-rest)
[![AppVeyor](https://ci.appveyor.com/api/projects/status/v6ycvs9nw6o2t821/branch/master?svg=true)](https://ci.appveyor.com/project/jimmidyson/syndesis-rest/)
[![Maven Central](https://img.shields.io/maven-central/v/io.syndesis/syndesis-rest.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22syndesisio%22%20AND%20a%3A%22syndesis-rest%22)
[![Dependency Status](https://dependencyci.com/github/syndesisio/syndesis-rest/badge)](https://dependencyci.com/github/syndesisio/syndesis-rest)

- [Building](#building)
- [Running](#run-in-development-mode)
- [Deploying](#deploying-to-kubernetes)
- [Endpoints](#endpoints)
- [Authentication](#authentication)

### Swagger
[![Swagger](http://dgrechka.net/swagger_validator_content_type_proxy.php?url=https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json)](https://online.swagger.io/validator/debug?url=https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json)

# Building

    mvn clean install

# Start a PostgreSQL DB in a Docker container

   docker run -d --rm \
              -p 5432:5432 \
              -e POSTGRES_USER=postgres \
              -e POSTGRES_PASSWORD=password \
              -e POSTGRES_DB=syndesis \
              postgres

# Deploying to Kubernetes

    oc login <KUBERNETES_MASTER>
    cd runtime
    mvn clean package fabric8:build fabric8:deploy fabric8:start

# Endpoints

* REST service: [http://localhost:8080/api/v1/](http://localhost:8080/api/v1/)
* Swagger doc:  [http://localhost:8080/api/v1/swagger.json](http://localhost:8080/api/v1/swagger.json)

There is a [staging URL](https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/) on OpenShift dedicated that you can test with, along with the [Swagger JSON](https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json) [![Swagger UI](http://petstore.swagger.io/images/logo_small.png)](http://petstore.swagger.io/?url=https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json).

This demo endpoint has some preloaded data and can be used for testing and demoing purposes.

# Authentication

The REST API is protected via an OAuth 2.0 Bearer Token. The REST API server is a resource server as defined in
[The OAuth 2.0 Authorization Framework: Bearer Token Usage](https://tools.ietf.org/html/rfc6750). This requires all requests
include a valid access token in the `Authorization` header of the request (there are other methods but they are discouraged).
The header should look like:

    Authorization: Bearer MDQyODExLCJpc3MiOi...

## Roland's random notes

* Create image

```
eval $(minishift docker-env)
cd runtime
mvn fabric8:build -Dfabric8.mode=kubernetes
```

Ignore warning about not being able to remove old image.

* Kill Pod for `syndesis-rest`

```
oc delete pod $(oc get pods | awk '{ print $1 }' | grep syndesis-rest)
```

* Edit config

```
oc edit cm syndesis-rest-config
```

* Get Token from developer-tool in Chrome when having the UI open

```
TOKEN="...."
```

* Call the REST API with curl

```
curl -k -v https://syndesis.192.168.64.3.xip.io/api/v1/integrations -H "Authorization: Bearer $TOKEN" -H "Accept: application/json" -H "Content-Type: application/json" -d '{ "name": "syndesis-test-repo" }'
```

* Port forward for remote debugging (set JAVA_OPTIONS in your DC to enable it)

```
oc port-forward $(oc get pods | awk '{ print $1 }' | grep syndesis-rest) 8000:8000
```
