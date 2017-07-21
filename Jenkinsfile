def mavenVersion='3.3.9'
//We need a node so that we can have access to environemnt variables.
//The allocated node will actually be the Jenkins master (which is expected to provide these variables) as long as it has available executors.
node {
    slave {
        withOpenshift {
            withMaven(
                mavenImage: "maven:${mavenVersion}",
                envVars: [
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_ID', value: "${env.GITHUB_OAUTH_CLIENT_ID}"),
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_SECRET', value: "${env.GITHUB_OAUTH_CLIENT_SECRET}")
                ],
                serviceAccount: "jenkins", mavenSettingsXmlSecret: 'm2-settings'
            ) {
                inside {
                    def testingNamespace = generateProjectName()

                    checkout scm

                    stage 'Build'
                    container(name: 'maven') {
                        sh "mvn -U clean install fabric8:build -Pci -Duser.home=/home/jenkins"
                    }

                    stage 'System Tests'
                    test(component: 'syndesis-rest', namespace: "${testingNamespace}", serviceAccount: 'jenkins')
                }
            }
        }
    }
}
