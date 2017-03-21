def mavenVersion='3.3.9'

slave {
    withOpenshift {
            withMaven(mavenImage: "maven:${mavenVersion}", serviceAccount: "jenkins", mavenRepositoryClaim: "m2-local-repo") {
                inside {
                    def testingNamespace = generateProjectName()

                    checkout scm

                    stage 'Build'
                    container(name: 'maven') {
                        sh "mvn clean install fabric8:build -Dfabric8.mode=openshift -Dfabric8.build.strategy=docker -Dfabric8.namespace=ipaas-ci"
                    }

                    stage 'System Tests'
                    test(component: 'ipaas-rest', namespace: "${testingNamespace}", serviceAccount: 'jenkins')
                 }

        }
    }
}
