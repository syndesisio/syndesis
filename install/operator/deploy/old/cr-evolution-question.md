While crafting a CSV for Syndesis/Fuse Online I found out that CR we are currently using needs to be adapted slightly to fit to UI convention when. I.e. we have to change

```yaml
apiVersion: "syndesis.io/v1alpha1"
kind: "Syndesis"
spec:
  components:
    db:
      resources:
        limits:
          memory: 255Mi
        volumeCapacity: 1Gi
  ...
```

to something like

```yaml
apiVersion: "syndesis.io/v1alpha2"
kind: "Syndesis"
spec:
  components:
    db:
      resources:
        limits:
          memory: 255Mi
      volumeCapacity: 1Gi
```

Note the changed indentation level of `volumeCapacity`. This change is required so that we could use an x-descriptor `urn:alm:descriptor:com.tectonic.ui:resourceRequirements` for the relevant UI meta data.

My question now is how such a CR schema evaluation can be managed by an operator.
The operator supporting the new `apiVersion` needs to be able to support both versions and start a migration to "syndesis.io/v1alpha2" when it finds a "syndesis.io/v1alpha1" version.

Is it possible for operator SDK to generate and support both versions, so that the operator can trigger the conversion ?




        -
