FROM fabric8/s2i-java:3.0-java8

ENV AB_JOLOKIA_HTTPS="true"

USER 0
# Setting holding all remote repository needed for building the sample projects
COPY settings.xml /tmp/settings.xml

# Maven dependencies will be downloaded with a 2 step approach.
# In the first step, the project use cached syndesis dependencies
# just to download majority of maven dependencies and caching this Docker layer.
# The second step takes care of installing the sample project using the latest
# dependencies freshly baked.

# Copy cached dependencies: used to cache Docker layer
COPY m2.cache /tmp/artifacts/m2/
COPY m2/project /tmp/artifacts/m2/project
# Download dependencies against cached Syndesis version: it creates a cached layer as well
RUN cd /tmp/artifacts/m2/project \
    && mvn -s /tmp/settings.xml verify -Dmaven.repo.local=/tmp/artifacts/m2 --fail-never

# Copy over all images from the current build.
# These are created with copy_mvn_repo.sh
# and contains all syndesis artefacts with the current version
# number (so also snapshot versions)
# Hence if you want the latest changes you need a rebuild, too
COPY m2 /tmp/artifacts/m2/

# Build the sample project which is has been generated with the servers'
# rest-builder-image-generator.jar (see syndesis-server)
# This generator works by creating a sample project/integration which references
# to every connector referenced in io/syndesis/server/dao/deployment.json
# This project is now compiled here in order to pick up all dependencies and store
# them in /tmp/artifacts/m2.
# This directory is used during an S2I build as the local maven repository, so everything
# should be then already prepopulated for the standard connectors delivered
# with Sydnesis.
RUN cd /tmp/artifacts/m2/project \
 && mvn --batch-mode -s /tmp/settings.xml -Dmaven.repo.local=/tmp/artifacts/m2 package -DskipTests -e -Dfabric8.skip=true \
 && rm -rf /tmp/artifacts/m2/project \
 && chgrp -R 0 /tmp/artifacts/m2 \
 && chmod -R g=u /tmp/artifacts/m2

# Copy licenses
RUN mkdir -p /opt/ipaas/
COPY lic* /opt/ipaas/

USER 1000