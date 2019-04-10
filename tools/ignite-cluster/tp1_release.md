# Release Syndesis TP1

This is the release plan for TP1, which consist of multiple distinct repos. This plan changes when going to a Monorepo, as many of the projects below then share the same version number (and release cycle)

## Tagging and Building

### External Images

* https://github.com/syndesisio/pemtokeystore
* https://github.com/syndesisio/keycloak-openshift

Those have fixed version numbers are not necessarily released afresh. They have
independent version numbers.

### Maven Dependencies

* Connectors
  - https://github.com/syndesisio/connectors (syndesis-rest, syndesis-verifier, atlasmap)

* Integration runtime
  - https://github.com/syndesisio/syndesis-integration-runtime (syndesis-rest)

* Atlasmap, release with version `$VERSION_ATLAS_MAP` (e.g. 1.30.0)
  - https://github.com/atlasmap/atlasmap
  - https://github.com/atlasmap/camel-atlasmap (syndesis-rest)

### Syndesis

* Build, release and push Docker images with version `$VERSION_SYNDESIS` (e.g. 1.0.0)
  - https://github.com/syndesisio/syndesis-rest
  - https://github.com/syndesisio/syndesis-ui
  - https://github.com/syndesisio/syndesis-verifier

* Build atlasmap as Docker images

* Check those, but not necessarily requires a new release (they are used as Maven deps only):
  - https://github.com/syndesisio/connectors (required by syndesis-rest, syndesis-verifier)
  - https://github.com/syndesisio/syndesis-integration-runtime (required by syndesis-rest)

### Templates

* Check `syndesis-template.go` for proper version numbers
* Run `./run.sh --product` in `generators`
* Commit and push
* Tag with release version number, e.g. `git tag 1.0.0` and push
* Run `./run.sh`
* Commit and push


## Import image and Templates in fuse-ignite-cluster

* Check that images are build with the Fuse [pipeline builds](https://fusesource-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/JBoss%20Fuse%207.0/job/ipaas-tp1/) and pushed to the [registry](https://registry-console.engineering.redhat.com/registry#/images/jboss-fuse-7-tech-preview)
* Import images in to cluster
  - Goto `syndesis-project`, cd to `tools/images`
  - Docker login to iginite-fuse cluster: `docker login -u $(oc whoami) -p $(oc whoami -t) registry.fuse-ignite.openshift.com`
  - Run import scripts: `./migrate.sh `
* Import template to fues-ignite namespace (set proper version number):

```
oc delete template syndesis-restricted -n fuse-ignite
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/fuse-ignite-1.0.0/syndesis-restricted.yml
```

* Try out release on a fresh project:

```
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/fuse-ignite-1.0.0/support/serviceaccount-as-oauthclient-restricted.yml

oc new-app fuse-ignite/syndesis-restricted \
    -p ROUTE_HOSTNAME=app-$(oc project -q).6a63.fuse-ignite.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)
```
