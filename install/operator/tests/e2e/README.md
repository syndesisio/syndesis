# E2E Testing
End-to-end (e2e) testing is automated testing for real user scenarios.

## Running the tests
### Prerequisites
- a running k8s cluster and kube config.
- [opearator sdk](https://github.com/operator-framework/operator-sdk) installed

### Running

You can run your local code with
```shell
operator-sdk test local ./tests/e2e --up-local --local-operator-flags "run --operator-config build/conf/config.yaml"
```

OR

Before running, replace `REPLACE_IMAGE` in the `deploy/operator.yaml` with the image you have built. You can then run from an existing docker image with:

```$xslt
test local ./tests/e2e
```
For a full list of available options have a look at the [SDK Cli reference](https://github.com/operator-framework/operator-sdk/blob/master/doc/sdk-cli-reference.md#test)
