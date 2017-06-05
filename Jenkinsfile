node {
    inNamespace(cloud: 'openshift', prefix: 'stest-selfcheck') {
    	stage 'Test templates'
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
