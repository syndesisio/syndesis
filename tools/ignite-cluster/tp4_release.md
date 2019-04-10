# Release Syndesis TP4

## Tagging and Building

* Community releases are build with https://ci.fabric8.io/view/release%20builds/job/syndesis-release/.
  Build it with `$VERSION_SYNDESIS`. Latest right now is 1.3.4, 1.3.x is reserved for TP4. The build is based on `syndesis release`

### Templates

* Create the productised templates locally and tag them in Git Hub

```
git co 1.3.4
syndesis release --product-templates --release-version 1.3.4 --verbose
```

This will create a tag `fuse-ignite-1.3` on GitHub which can be used to refer to the productised template

_**TODO**: We should put this into a Jenkins job, too_

## Import image and Templates in fuse-ignite-cluster

* Verify that the productised builds are created and pick up the version numbers from https://docs.google.com/spreadsheets/d/1ohR6poCaYmQ7Ga3OWgSd189sT8MlXexDCZbmb_SY994/edit#gid=0

* Import images in to cluster

```
# Verify that you are in the VPN so that you can reach the brew registry

# Login into fuse-ignite cluster
docker login -u $(oc whoami) -p $(oc whoami -t) registry.fuse-ignite.openshift.com

# Edit migrate-images.pl and update coordinates
cd tools/ignite-cluster
vi migrate-images.pl

# Run script
perl migrate-images.pl

# Check that all images has been imported correctly

oc get is -n fuse-ignite
```

* Ensure that all images streams have been created properly in the namespace fuse-ignite:
  - fuse-ignite-java-openshift
  - fuse-ignite-server
  - fuse-ignite-ui
  - fuse-ignite-meta
  - oauth-proxy
  - prometheus

* Import template to fuse-ignite namespace:

```
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis/fuse-ignite-1.3/install/syndesis.yml -n fuse-ignite
```

* Try out release on a fresh project (note the version number in the template name):

```
oc create -f https://raw.githubusercontent.com/syndesisio/syndesis/fuse-ignite-1.3/install/support/serviceaccount-as-oauthclient-restricted.yml

oc new-app fuse-ignite/syndesis-fuse-ignite-1.3 \
    -p ROUTE_HOSTNAME=app-$(oc project -q).6a63.fuse-ignite.openshiftapps.com \
    -p OPENSHIFT_MASTER=$(oc whoami --show-server) \
    -p OPENSHIFT_PROJECT=$(oc project -q) \
    -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client)
```

* Important: Communicate to OpenShift Online Team to update the provisioning script to point to the tagged github URL
