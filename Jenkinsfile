@Library('github.com/redhat-ipaas/ipaas-pipeline-library@master')
def mavenVersion='3.3.9'

slave {
    withOpenshift {
            withMaven(mavenImage: "maven:${mavenVersion}", serviceAccount: "jenkins") {
                inside {
                            checkout scm

                            stage 'Build'
                            container(name: 'maven') {
                                sh "mvn clean install"
                            }
                }
            }
    }
}
