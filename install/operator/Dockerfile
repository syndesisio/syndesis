FROM centos:7

RUN adduser -r syndesis-operator
USER syndesis-operator

ADD syndesis-operator /usr/local/bin/syndesis-operator

ENTRYPOINT "/usr/local/bin/syndesis-operator"
