FROM fabric8/s2i-java:latest

ADD project /tmp/project
RUN /bin/bash /tmp/project/setup.sh

USER root
RUN rm -rf /tmp/project
USER 1000
