node {
    stage 'Validate system test scripts via arquillian steps'
    inNamespace(cloud: 'openshift', prefix: 'stest-selfcheck') {
    	checkout scm
    	createEnvironment(
	  cloud: 'openshift',
          environmentSetupScriptUrl: "file:${WORKSPACE}/src/test/resources/setup.sh",
          environmentTeardownScriptUrl: "file:${WORKSPACE}/src/test/resources/teardown.sh", 
          waitForServiceList: ['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak', 'syndesis-verifier'],
          waitTimeout: 600000L,
	  namespaceCleanupEnabled: false,
	  namespaceDestroyEnabled: false)
    }
}
