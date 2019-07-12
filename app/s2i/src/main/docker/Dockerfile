FROM fabric8/s2i-java:3.0-java8

ENV AB_JOLOKIA_HTTPS="true"

# Setting holding all remote repository needed for building the sample projects
COPY settings.xml /tmp/settings.xml

# We verify a base generated project from a previous stable version in order to download
# and cache the majority of dependencies used by the project
COPY m2/project-cache /tmp/artifacts/m2/project-cache
USER 0
RUN cd /tmp/artifacts/m2/project-cache \
    && mvn -s /tmp/settings.xml verify -Dmaven.repo.local=/tmp/artifacts/m2 --fail-never

# Copy over all images from the current build.
# These are created with copy_mvn_repo.sh
# and contains all syndesis artefacts with the current version
# number (so also snapshot versions)
# Hence if you want the latest changes you need a rebuild, too
COPY m2 /tmp/artifacts/m2/

# Script that will clean the cached project dependencies not needed by latest base project version
# WARNING: We better execute the cleanup here to safely remove any dependency not needed by the base project
# If by any chance this script remove needed dependencies, the mvn build after this will put them in again!
COPY clean_diff_dependencies.sh /tmp/clean_diff_dependencies.sh
RUN mvn -f /tmp/artifacts/m2/project-cache/pom.xml -s /tmp/settings.xml dependency:list -Dmaven.repo.local=/tmp/artifacts/m2 -Dsort=true > /tmp/project-cache-dependency-log \
    && mvn -f /tmp/artifacts/m2/project/pom.xml -s /tmp/settings.xml dependency:list -Dmaven.repo.local=/tmp/artifacts/m2 -Dsort=true > /tmp/project-dependency-log \
    && chmod +x /tmp/clean_diff_dependencies.sh \
    && /tmp/clean_diff_dependencies.sh /tmp/project-cache-dependency-log /tmp/project-dependency-log /tmp/artifacts/m2

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