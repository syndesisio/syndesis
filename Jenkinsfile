@Library('github.com/syndesisio/syndesis-pipeline-library@master')
def params = [:]

node {

    def branch = "${env.BRANCH_NAME}"
    echo "Using branch: ${branch}."

    slave {
        withArbitraryUser {
            withSshKeys {
                withOpenshift {
                    withMaven(serviceAccount: 'jenkins', mavenSettingsXmlSecret: 'm2-settings') {
                        withYarn {
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
        }
    }
}
