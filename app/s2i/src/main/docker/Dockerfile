FROM fabric8/s2i-java:3.0-java8

ENV AB_JOLOKIA_HTTPS="true"

# Setting local+remote repositories needed for building the sample projects
ADD --chown=0 settings.xml /tmp/settings.xml

# Copy over all local dependencies to docker maven repo
# The integration expects all dependencies in /tmp/artifacts/m2
# so you cannot change the location of the local repo!
ADD --chown=1000 repository /tmp/artifacts/m2

# Copy licenses
ADD --chown=0 licenses* /opt/ipaas/
