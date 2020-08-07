FROM fabric8/s2i-java:3.1-java11

ENV AB_JOLOKIA_HTTPS="true"

USER 0

# Setting local+remote repositories needed for building the sample projects
ADD settings.xml /tmp/settings.xml
RUN chown 0 /tmp/settings.xml

# Copy over all local dependencies to docker maven repo
# The integration expects all dependencies in /tmp/artifacts/m2
# so you cannot change the location of the local repo!
ADD repository /tmp/artifacts/m2
RUN chown -R 1000 /tmp/artifacts/m2

# Copy licenses
ADD licenses* /opt/ipaas/
RUN chown -R 0 /opt/ipaas/

USER 1000
