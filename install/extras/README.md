# Syndesis OpenShift Templates not included in the default installation

The files in this directory are optional components of syndesis, that aren't installed by default via the syndesis installer. If you wish to have any of these components installed, just run the following command:

```
oc create -f some_component.yml
```