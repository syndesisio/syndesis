def mavenVersion='3.3.9'
node {
    slave {
        withOpenshift {
                withMaven(mavenImage: "maven:${mavenVersion}", 
		envVars: [ containerEnvVar(key:'GITHUB_OAUTH_CLIENT_ID', value: "${env.GITHUB_OAUTH_CLIENT_ID}"), containerEnvVar(key:'GITHUB_OAUTH_CLIENT_SECRET', value: "${env.GITHUB_OAUTH_CLIENT_SECRET}") ], 
		serviceAccount: "jenkins", mavenRepositoryClaim: "m2-local-repo", mavenSettingsXmlSecret: 'm2-settings') {
                    inside {
                        def testingNamespace = generateProjectName()
                        checkout scm

                         stage 'Build'
                         container(name: 'maven') {
                             sh "mvn clean install fabric8:build -Pci -Duser.home=/home/jenkins"
                         }

                         stage 'System Tests'
                         test(component: 'syndesis-rest', namespace: "${testingNamespace}", serviceAccount: 'jenkins')
                      }
             }
         }
      }     
}
