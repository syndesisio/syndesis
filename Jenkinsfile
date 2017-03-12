def mavenVersion='3.3.9'

slave {
    withOpenshift {
            withMaven(mavenImage: "maven:${mavenVersion}", serviceAccount: "jenkins", mavenRepositoryClaim: "m2-local-repo") {
                inside {
                    checkout scm
                    stage 'Build'
                    container(name: 'maven') {
                        sh "mvn clean install fabric8:build -Dfabric8.mode=openshift -Dfabric8.build.strategy=docker -Dfabric8.namespace=ipaas-testing"
                    }

                    stage 'System Tests'
                    test(component: 'ipaas-rest', version: "${version}", namespace: 'ipaas-testing', serviceAccount: 'jenkins')

                    stage 'Rollout'
                    tag(imageStream: 'ipaas-rest')
                    rollout(deploymentConfig: 'ipaas-rest', namespace: 'ipaas-staging')
                 }

        }
    }
}
