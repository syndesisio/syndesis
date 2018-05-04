FROM fabric8/s2i-java:2.1.0

ENV AB_JOLOKIA_HTTPS="true"

COPY m2 /tmp/artifacts/m2/

COPY settings.xml /tmp/settings.xml

USER 0

RUN cd /tmp/artifacts/m2/project \
 && mvn --batch-mode -s /tmp/settings.xml -Dmaven.repo.local=/tmp/artifacts/m2 package -DskipTests -e -Dfabric8.skip=true \
 && rm -rf /tmp/artifacts/m2/project \
 && chgrp -R 0 /tmp/artifacts/m2 \
 && chmod -R g=u /tmp/artifacts/m2

# Copy licenses
RUN mkdir -p /opt/ipaas/
COPY lic* /opt/ipaas/

USER 1000
