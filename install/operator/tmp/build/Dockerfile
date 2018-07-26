FROM alpine:3.6

RUN adduser -D syndesis-operator
USER syndesis-operator

ADD tmp/_output/bin/syndesis-operator /usr/local/bin/syndesis-operator
