@Library('github.com/redhat-ipaas/ipaas-pipeline-library@master')
def mavenVersion='3.3.9'

mavenNode(mavenImage: "maven:${mavenVersion}") {
    checkout scm

    stage 'Build'
    container(name: 'maven') {
        sh "mvn clean install"
    }
}
