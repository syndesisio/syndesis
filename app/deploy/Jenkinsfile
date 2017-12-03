node {
  stage('Validate templates') {
    slave {
      withOpenshift {
        withGo {
          inside {
            checkout scm
            container(name: 'go') {
              sh "support/validate-generated-templates.sh"
            }
          }
        }
      }
    }
  }

  stage('Test templates') {
    inNamespace(cloud: 'openshift', prefix: 'template-testing') {
      checkout scm
      createEnvironment(
        cloud: 'openshift',
        scriptEnvironmentVariables: ['OPENSHIFT_TEMPLATE_FROM_WORKSPACE': 'true', 'SYNDESIS_TEMPLATE_TYPE': 'syndesis-ci', 'WORKSPACE': "$WORKSPACE"],
        environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
        environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
        waitForServiceList: ['syndesis-rest', 'syndesis-ui', 'syndesis-verifier'],
        waitTimeout: 600000L,
        namespaceCleanupEnabled: false,
        namespaceDestroyEnabled: false
      )
    }
  }
}
