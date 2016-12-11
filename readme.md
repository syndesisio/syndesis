# IPaaS API

- [Building](#building)
- [Running](#run-in-development-mode)
- [Deploying](#deploying-to-kubernetes) 
- [Endpoints](#endpoints)

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
