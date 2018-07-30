FROM centos:7

RUN adduser -r syndesis-operator
USER syndesis-operator

# Use that template for creating the operator
ADD syndesis-template.yml /syndesis-template.yml

# Add the operator
ADD syndesis-operator /usr/local/bin/syndesis-operator

ENTRYPOINT [ "/usr/local/bin/syndesis-operator", "-template", "/syndesis-template.yml" ]
