node {

    def branch = "${env.BRANCH_NAME}"
    echo "Using branch: ${branch}."

    stage ('Load pipeline library') {
        checkout scm
        sh 'git submodule update --init ci'
        library identifier: "local-pipeline-library@${env.BRANCH_NAME}", retriever: workspaceRetriever("${WORKSPACE}/ci/pipeline-library")
    }

    uberPod(image: 'syndesis/jenkins-slave-full-centos7:1.0.8', serviceAccount: 'jenkins', mavenSettingsXmlSecret: 'm2-settings')  {
        withArbitraryUser {
            withSshKeys {
                inside {
                    checkout scm
                    sh "./build.sh --with-image-streams"
                    stage('System Tests') {
                        test(component: 'syndesis', serviceAccount: 'jenkins')
                    }
                    if ("master" == branch) {
                        stage('Rollout') {
                            tag(sourceProject: 'syndesis-ci', imageStream: 'syndesis-rest')
                            rollout(deploymentConfig: 'syndesis-rest', namespace: 'syndesis-staging')

                            tag(sourceProject: 'syndesis-ci', imageStream: 'syndesis-ui')
                            rollout(deploymentConfig: 'syndesis-ui', namespace: 'syndesis-staging')
                        }
                    } else {
                        echo "Branch: ${branch} is not master. Skipping rollout"
                    }
                }
            }
        }
    }
}
