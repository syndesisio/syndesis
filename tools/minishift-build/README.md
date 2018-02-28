# Build Scripts for Minishift

If you have a build environment based on minishift and created as described in the [Quickstart instructions](https://syndesis.io/quickstart/) with the OpenShift template `syndesis-dev`, then you can use these scripts to update your installations with the latest code available on GitHub.

These scripts assume the following setup:

* You have your forks of the following repos checked out in one directory with the names:
  - syndesis-integration-runtime
  - syndesis-rest
  - syndesis-ui
  - syndesis-verifier
  - atlasmap
* You have a Git remote "upstream" added to all of these local clones. This remote should point to the upstream git repo
* You create a file "root_dir" within this directory and which points to the top-level directory where you have your repositories checked out.
* You need the `realpath` command (which can be installed from brew when on a Mac)

Then you can use the 'build-all.sh' script which does the following:

* Change openshift to context "minishift"
* Login into openshift as developer/developer
* Eval the docker environment for the minishift docker daemon
* Go into each directory, git pull from upstream and rebase the current branch on upstream/master
* Run the Maven / Yarn builds in all repositories
* Create the Docker images in the minishift's Docker daemon
* Kills the running pods for these services
* Start a `watch oc get pods` which you can kill.

You can also symlink it into a directory which is on your `$PATH` for ease of access.
