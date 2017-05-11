cube.namespace().withCloud('openshift').withPrefix('stest-selfcheck').inside {

node {
    stage 'Test templates'
    checkout scm
    cube.environment()
          .withSetupScriptUrl("file:///${WORKSPACE}/src/test/resources/setup.sh")
          .withTeardownScriptUrl("file:///${WORKSPACE}/src/test/resources/teardown.sh")
          .withServicesToWait(['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak', 'syndesis-verifier'])
          .withWaitTimeout(600000L)
          .create()
    }
}
