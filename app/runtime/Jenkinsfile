def mavenVersion='3.5.0'
//We need a node so that we can have access to environemnt variables.
//The allocated node will actually be the Jenkins master (which is expected to provide these variables) as long as it has available executors.
node {
    slave {
        withOpenshift {
            withMaven(
                    mavenImage: "maven:${mavenVersion}",
                    serviceAccount: "jenkins", mavenRepositoryClaim: "m2-local-repo", mavenSettingsXmlSecret: 'm2-settings'
            ) {
                inside {
                    checkout scm

                    stage 'Build'
                    container(name: 'maven') {
                        sh "mvn clean install -Duser.home=/home/jenkins"
                    }
                }
            }
        }
    }
}
