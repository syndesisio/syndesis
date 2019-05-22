FROM centos:7

RUN adduser -r syndesis-operator
USER syndesis-operator

# Use that template for creating the operator
ADD syndesis-template.yml /conf/syndesis-template.yml

# Add addons directory
ADD addons /conf/addons

# Add the operator
ADD syndesis-operator /usr/local/bin/syndesis-operator

ENTRYPOINT [ "/usr/local/bin/syndesis-operator", "--template", "/conf/syndesis-template.yml" ]
