# Release Syndesis TP2

This is the release plan for TP2, which consist of multiple distinct repos. This plan changes when going to a Monorepo, as many of the projects below then share the same version number (and release cycle)

## Tagging and Building

### Maven Dependencies

* Connectors via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/connectors-release/
  - https://github.com/syndesisio/connectors (syndesis-rest, syndesis-verifier, atlasmap)

* Integration runtime via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/syndesis-integration-runtime-release/
  - https://github.com/syndesisio/syndesis-integration-runtime (syndesis-rest)

* Atlasmap, release with version `$VERSION_ATLAS_MAP` (e.g. 1.31.0)
  - https://github.com/atlasmap/atlasmap via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/atlasmap-release/ This will also build the atlasmap/atlasmap images.
  - https://github.com/atlasmap/camel-atlasmap (required by syndesis-rest) via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/camel-atlasmap-release/
 
### Syndesis

* Build, release and push Docker images with version `$VERSION_SYNDESIS` (e.g. 1.1.0)
  - https://github.com/syndesisio/syndesis-rest via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/syndesis-rest-release/
  - https://github.com/syndesisio/syndesis-ui (we should automate this)
    * Edit pom.xml, set version number
    * Commit to master
    * Git tag & push
    * Change back to snapshot version number in pom.xml and commit
  - https://github.com/syndesisio/syndesis-verifier via https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/syndesis-verifier-release/

* Check those, but not necessarily requires a new release (they are used as Maven deps only):
  - https://github.com/syndesisio/connectors (required by syndesis-rest, syndesis-verifier)
  - https://github.com/syndesisio/syndesis-integration-runtime (required by syndesis-rest)

### Templates

* Check `syndesis-template.go` for proper version numbers
* Run `./run.sh --product` in `generators`
* Commit and push
* Tag with release version number, e.g. `git tag 1.1.0` and push
* Run `./run.sh`
* Commit and push


## Import image and Templates in fuse-ignite-cluster

* Check that images are build with the Fuse [pipeline builds](https://fusesource-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/JBoss%20Fuse%207.0/job/ipaas-tp1/) and pushed to the [registry](https://registry-console.engineering.redhat.com/registry#/images/jboss-fuse-7-tech-preview)
* Import images in to cluster
  - Goto `syndesis-project`, cd to `tools/images`
  - Docker login to iginite-fuse cluster: `docker login -u $(oc whoami) -p $(oc whoami -t) registry.fuse-ignite.openshift.com`
  - Run import scripts: `./migrate.sh`
* Ensure that all images streams have been created properly in the namespace fuse-ignite:
  - fuse-ignite-java-openshift      
  - fuse-ignite-mapper
  - fuse-ignite-rest
  - fuse-ignite-ui
  - fuse-ignite-verifier
  - oauth-proxy

* Import template to fues-ignite namespace:

```
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/fuse-ignite-1.1.0/syndesis-restricted.yml
```

* Try out release on a fresh project (note the version number in the template name):

```
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/fuse-ignite-1.1.0/support/serviceaccount-as-oauthclient-restricted.yml

oc new-app fuse-ignite/syndesis-restricted-1.1 \
    -p ROUTE_HOSTNAME=app-$(oc project -q).6a63.fuse-ignite.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)
```

* Important: Communicate to OpenShit Online Team to update the provisioning script to point to the tagged github URL 
