FROM /Users/chirino/sandbox/syndesis-s2i-image/templates/../rhel

ADD project /tmp/project
RUN /bin/bash /tmp/project/setup.sh

USER root
RUN rm -rf /tmp/project
USER 1000
