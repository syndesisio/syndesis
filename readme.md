# IPaaS API

- [Building](#building)
- [Deploying](#deploying)    

# Building

    mvn clean install
    
# Deploying    

    cd runtime
    mvn clean package fabric8:build fabric8:deploy fabric8:start
    