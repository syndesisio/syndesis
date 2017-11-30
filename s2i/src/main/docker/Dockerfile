FROM fabric8/s2i-java:2.0.2

RUN mkdir -p /tmp/artifacts
ADD repo /tmp/artifacts/m2
ADD project /deployments/project
USER root
RUN chown -R jboss /deployments/project /tmp/artifacts/m2
USER 1000

RUN \
  cd /deployments/project; \
  mvn --batch-mode -Dmaven.repo.local=/tmp/artifacts/m2 package -DskipTests -e -Dfabric8.skip=true; \
  rm -rf /deployments/project;

