FROM fabric8/s2i-java:2.0.2

COPY m2 /tmp/artifacts/m2/

USER 0

RUN cd /tmp/artifacts/m2/project \
 && mvn --batch-mode -Dmaven.repo.local=/tmp/artifacts/m2 package -DskipTests -e -Dfabric8.skip=true \
 && rm -rf /tmp/artifacts/m2/project \
 && chgrp -R 0 /tmp/artifacts/m2 \
 && chmod -R g=u /tmp/artifacts/m2

USER 1000

