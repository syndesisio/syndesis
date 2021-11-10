FROM fabric8/s2i-java:3.0-java8

ENV AB_JOLOKIA_HTTPS="true"

USER 0
# Setting local+remote repositories needed for building the sample projects
ADD maven/settings.xml /tmp/settings.xml

# Copy over all local dependencies to docker maven repo
# The integration expects all dependencies in /tmp/artifacts/m2
# so you cannot change the location of the local repo!
ADD maven/repository /tmp/artifacts/m2

RUN chgrp -R 0 /tmp/artifacts/m2 \
 && chmod -R g=u /tmp/artifacts/m2

# Copy licenses
RUN mkdir -p /opt/ipaas/
ADD maven/licenses* /opt/ipaas/

USER 1000
