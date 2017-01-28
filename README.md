# Red Hat iPaaS OpenShift Templates

This repository contains a simple way to get the Red Hat iPaaS deployed, using OpenShift templates.

Run the following commands:

```bash
oc create -f https://raw.githubusercontent.com/redhat-ipaas/openshift-templates/master/redhat-ipaas.yml
oc new-app redhat-ipaas -p ROUTE_HOSTNAME=<EXTERNAL_HOSTNAME>
```

Replace `EXTERNAL_HOSTNAME` with a value that will resolve to the address of the OpenShift router.

Once all pods are started up, you should be able to access the iPaaS at `https://<EXTERNAL_HOSTNAME>/`.
