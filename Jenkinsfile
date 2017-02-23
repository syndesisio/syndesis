@Library('github.com/redhat-ipaas/ipaas-pipeline-library@master')
def mavenVersion='3.3.9'

mavenNode(mavenImage: "maven:${mavenVersion}") {
    checkout scm

    stage 'Build'
    container(name: 'maven') {
        sh "ls -al"
        pom = readMavenPom(file: 'pom.xml')
        version = pom.version.replaceAll("SNAPSHOT", "${versionSuffix}")
        sh "mvn clean install"
    }
}
