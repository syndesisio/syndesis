cube.namespace().withCloud('openshift').withPrefix('template-testing').inside {

node {
    stage 'Test templates'
    checkout scm
    withEnv(['OPENSHIFT_TEMPLATE_FROM_WORKSPACE=true']) {
          cube.environment()
            .withSetupScriptUrl('https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh')
            .withTeardownScriptUrl('https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh')
            .withServicesToWait(['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak', 'syndesis-verifier'])
            .withWaitTimeout(1200000L)
            .create()
          }
    }
}
