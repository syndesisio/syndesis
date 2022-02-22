FROM ${java.base.image}

ENV AB_JOLOKIA_HTTPS="true"

# Setting local+remote repositories needed for building the sample projects
ADD --chown=jboss:jboss maven/settings.xml /tmp/settings.xml

# Copy over all local dependencies to docker maven repo
# The integration expects all dependencies in /tmp/artifacts/m2
# so you cannot change the location of the local repo!
RUN mkdir -p /tmp/artifacts && chown jboss:jboss /tmp/artifacts
ADD --chown=jboss:jboss maven/repository /tmp/artifacts/m2

# Copy licenses
RUN mkdir -p /opt/ipaas/
ADD maven/licenses* /opt/ipaas/
