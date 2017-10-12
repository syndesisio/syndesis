FROM fabric8/s2i-java:2.0.2

ADD project /deployments/project
USER root
RUN chown -R jboss /deployments/project
USER 1000

RUN \
  cd /deployments/project; \
  mvn clean package ; \
  rm -rf /deployments/project;

