# Syndesis Show and tell notes:

##### Show progress at bootstrap
- you can tell `minishift` to show you progresses of its bootstrap with flag `--show-libmachine-logs`

#####  Configure caching for Docker Images
- you can enable a local Docker mirror (started as a docker container on your host) to provide a fast cache for all the images you have to download :
```
atlasmap/atlasmap:$LATEST
centos/php-70-centos7:$LATEST
syndesis/syndesis-ui:$LATEST
syndesis/syndesis-verifier:$LATEST
centos/postgresql-95-centos7:$LATEST
openshift/oauth-proxy:$OAUTH_VERSION
openshift/origin-sti-builder:$OC_VERSION
openshift/origin-deployer:$OC_VERSION
openshift/origin-docker-registry:$OC_VERSION
openshift/origin-haproxy-router:$OC_VERSION
openshift/origin:$OC_VERSION
openshift/origin-pod:$OC_VERSION
```

```bash
DOCKER_REGISTRY_CACHE_FOLDER="/500GB/fabric8_vm_cache/docker_registry_cache"
docker run -p 5000:5000 -d --restart=always \
            --name registry_cache \
            -e REGISTRY_PROXY_REMOTEURL=http://registry-1.docker.io \
            -v $DOCKER_REGISTRY_CACHE_FOLDER:/var/lib/registry:Z \
            registry:2
```
- to enable the local mirror in Minishift pass this flag: `--registry-mirror http://$(ip a s wlp4s0 | grep "inet "  | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" | head -1):5000` , where the complex expression is just a way to extract an ip address that the VM is able to reach. I use my Wifi assigned ones, but this depends on the complexity of your network configuration. At the end it's just a matter of providing something like: `--registry-mirror http://192.168.1.2:5000`

##### Install companion script
- there is an useful management scripts hiddeen in `syndesis-ui` repo: https://github.com/syndesisio/syndesis-ui/blob/master/scripts/syndesis-install providing a set of options around the usual steps defined in the quickstarts to bring up an env
- useful options are:
  - `--pull` to download all the images you need
  - `--clean` to delete the `project` at Openshift level (so that you can re-run a `oc new-app`)
  - `--from` to load Openshift definition files from a local folder you specify
  - `--namespace` to change the namespace to install Syndesis into
  - `--hostname` to specify an hostname for your vm

##### Bring up minishift console
- `minishift console` or `minishift status`

## Work on a localy copy of syndesis-rest
- from `syndesis-rest/README.md`:
```bash
# create image
eval $(minishift docker-env)
cd runtime
# build code and produce corresponding docker images
mvn fabric8:build -Dfabric8.mode=kubernetes

# now redploy the rest pod (either from cli or from web)
oc rollout latest dc/syndesis-rest
```

##### Collection of helper scripts to automaticlaly build locally and deploy to minishift
- they build AND deploy
- in `syndesis-project/tools/minishift-build/`:
```bash
atlasmap.sh
build-all.sh
integration-runtime.sh
README.md
rest.sh
ui.sh
vars.sh
verifier.sh
```



## Web development
- based on the idea of modify a service (route) definition in Syndesis Openshift config, so that it will route the invocation to the local dev webeserver running on host
- this happens in this script: `scripts/minishift-setup.sh`
0. install `yarn`
```bash
sudo dnf config-manager --add-repo https://dl.yarnpkg.com/rpm/yarn.repo
sudo dnf install -y yarn
```
1. build `syndesis-ui`
```
cd syndesis-ui
yarn install
cd scripts
sh syndesis-install
sh minishift-setup.sh
```
2. in another terminal
```
cd syndesis-ui
yarn start:minishift
# takes a while
```
3. (eventually) run tests
```
yarn test
```
4. Smoke test for development
```
# edit src/scss/_overrides.scss and add some flashy rule, like
* { background: red; }
# save and refresh the browser window

```


## Verifier
- same worfklow as rest layer if you have to test it in integration with everything else **BUT..**
- it works well in standalone, doesn't really requires to talk to any other service. It can then be run in full isolation on the host, either from within the IDE or from commandline with `mvn springboot:run` and you can interact with it with plain unauthenticated `curl` invocations.

## Atlasmap

##### In standalone mode:
```bash
git clone https://github.com/atlasmap/atlasmap-ui.git
git clone https://github.com/atlasmap/atlasmap-runtime.git
cd atlasmap-runtime
mvn clean install -DskipTests
cd runtime/
# itests profile enables debug mode, listining on port 8000 for a java debugger, and changes the default port so that atlasmap-ui standalone can connect to it
mvn -Pitests spring-boot:run

# leave the runtime running, and in another shell
cd ../../
cd atlasmap-ui
yarn install ; yarn start
# it will open a browser tab, connected to the standalone runtime you can interact with
# it will save the corresponding xml mapping file in `atlasmap/atlasmap-runtime/runtime/target/mappings/` where you can inspect the produced file 
```

###### In full integration with Syndesis
- ends up using the steps for Web Development 
```bash
cd atlasmap-ui
yarn link # understand the output
cd ../syndesis-ui
# based on the output of `yarn link`
yarn link @atlasmap/atlasmap.data.mapper
# you can now verify that nodes_modules/@atlasmap/atlasmap.data.mapper is now a symlink to the other folder on your system
# if you now follow the UI local dev guide, your UI will be loading (and rebuilding automatically) your local dev folder for atlasmap

```


## Debugging tips

##### attach a remote debugger to processes running on openshift
- quick and dirty: edit `DeploymentConfig` in the UI, altering the value for env var `JAVA_OPTIONS` to add the jvm standard flags to enable remote connections. This operation will recreate the pod, since you have altered start time env vars.
- or if you want to do it from CLI:
```bash
# or just use the alternative
oc env dc/syndesis-rest JAVA_DEBUG=true
# in case you need to append env vars values this is the pattern
# oc env dc/syndesis-rest JAVA_OPTIONS="$(oc env dc/syndesis-rest --list |  grep -P '(?<=JAVA_OPTIONS=)(.*)' -o --color=never) -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```
- now enable port forwarding:
```
oc port-forward $(oc get pods | grep rest- | awk '{print $1}') 5005
```
- and you can now use localhost:5005 as your remote debug endpoint in your IDE

###### general debugging tips
- for breakpoints suspend on  thread instead of global breakpoints (halts just the threads running that line of code, doesn't pause the whole jvm process. )


##### work with camel connectors
- connectors are just camel components, so you can test them easily locally in a driver project that just uses them.
- a good starting point is the `examples` directory  in the `connectors` folder
- to see them in Syndesis:
  - Zoran does a complicated thing: serves his `.m2/repository` folder over http, shares it over the internet and refer with `ngrok` and uses an instance of Nexus, in minishift, to proxy to that. Then he configures Syndesis to point to his Nexus. It's probably not really needed =P
  
## Openshift templates
- in repo `syndesis-templates`
- they are code generated, so if you want to change anything in them you should change the source template and not the `.yml` you import directly
- go in `generator/` folder and modify the corresponding template
- process the templates with `generator/run.sh`. Note: `golang` is a requirement for the generator script to work.
- after you have modified the files, if you want to use the helper script you can do
```bash
syndesis-ui/scripts/syndesis-install --clean --form .
```

## Source to images tips
##### fabric8 maven plugin in watch mode
???

##### Spring Boot Automatic Restarts
- the idea is that a running container instance can update it's content at runtime, without the need to produce a new `Image` and redeploy it, based on `oc rsync -f` capability.
- from https://github.com/fabric8io-images/s2i/tree/master/java/images/jboss
> This image also supports detecting jars with Spring Boot devtools included, which allows automatic restarts when files on the classpath are updated. Files can be easily updated in OpenShift using command oc rsync.
- Follow the instruction in `README.MD`. 



## Invoke REST APIs from `curl`
Current configuration doesn't allow that, but the template can be easily modified, at Syndesis deploy time, to enable it:
 
```bash
oc login -u system:admin
# add some permission to a system user
oc adm policy add-cluster-role-to-user system:auth-delegator -z default
oc login -u developer

# this just add this line to the pod configuration
#    --openshift-delegate-urls={"/api":{"resource":"projects","verb":"get","resourceName":"${OPENSHIFT_PROJECT}","namespace":"${OPENSHIFT_PROJECT}"}}
#
oc create -f <( sed -E -e  's#((\s+)- --pass-access-token)#\1\n\2- --openshift-delegate-urls={"/api":{"resource":"projects","verb":"get","resourceName":"${OPENSHIFT_PROJECT}","namespace":"${OPENSHIFT_PROJECT}"}}#' <(curl https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-dev-restricted.yml 2>/dev/null) )

```

Now you can use curl or this helper bash function:

```bash
curlsyn() {
	OS_TOKEN=$(oc whoami -t);
    curl -L --insecure -N -H "Authorization: Bearer $OS_TOKEN" -H "X-Forwarded-Access-Token: $OS_TOKEN"  2> /dev/null "$@"
}

# sample invocation
curlsyn https://syndesis.$(minishift ip).nip.io/api/v1/connections  

{"items":[{"connectorId":"sql-stored-connector","configuredProperties":{"password":"»ENC:7b47cd269b1514981440966f0d3d99f545758ec6e18c88e6864f7e86210457ac2497dca5ac72f672db4bd55a7d54d1aa","schema":"sampledb","url":"jdbc:postgresql://syndesis-db:5432/sampledb","user":"sampledb"},"options":{},"icon":"fa-database","description":"Sample Database Connection for Stored Procedure Invocation","id":"5","tags":["sampledb"],"name":"PostgresDB","isDerived":false}],"totalCount":1}
```

see comment here: https://github.com/syndesisio/syndesis/issues/99#issuecomment-348017381


## Delete `syndesis-db` content
If you need to force `syndesis-db` to delete it's content and having it recreated upon a `syndesis-rest` pod restart, you can invoke this one liner:
```bash
oc exec -it  $(oc get pods --selector=deploymentconfig=syndesis-db  -o jsonpath="{..metadata.name}") -- bash -c 'psql -d syndesis -c "DELETE FROM jsondb;"'
```

## Remove health and readiness probes
During development, readiness and health probes might get into your way, if you are using a debugger. For example, if you block the execution of a jvm process with a break point, the process might fail to respond to health check pings; if this happens, OpenShift will consider the pod as being unhealthy and it will kill it and deploy a new one.  
You can patch the `DeploymentConfig` definitions to disable these checks.

```bash
# disable health and readiness probes
for DC in syndesis-atlasmap syndesis-rest syndesis-ui
do
oc patch dc $DC  --type json   -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]'
oc patch dc $DC  --type json   -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]'
done

# or pick the ones that you need here
# oc patch dc syndesis-atlasmap --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]
# oc patch dc syndesis-atlasmap --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]
# oc patch dc syndesis-rest --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]
# oc patch dc syndesis-rest --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]
# oc patch dc syndesis-ui --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/livenessProbe"}]
# oc patch dc syndesis-ui --type json -p=[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]
```