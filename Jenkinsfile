node {
  inNamespace(cloud: 'openshift', prefix: 'template-testing') {
    stage 'Test templates'
    checkout scm
    createEnvironment(
      cloud: 'openshift',
      scriptEnvironmentVariables: ['OPENSHIFT_TEMPLATE_FROM_WORKSPACE': 'true', 'SYNDESIS_TEMPLATE_TYPE': 'syndesis-ci', 'WORKSPACE': "$WORKSPACE"],
      environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
      environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
      waitForServiceList: ['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak', 'syndesis-verifier'],
      waitTimeout: 600000L,
      namespaceCleanupEnabled: false,
      namespaceDestroyEnabled: false
    )
  }
}
