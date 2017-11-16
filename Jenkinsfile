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
                                stage ('update submodules') {
                                    sh 'git submodule update --init --recursive'
                                }
                                stage ('build connectors') {
                                    container('maven') {
                                        sh """
                                    cd connectors
                                    mvn -B -U clean install
                                    """
                                    }
                                }
                                stage ('build verifier') {
                                    container('maven') {
                                        sh """
                                    cd verifier
                                    mvn -B -U clean install
                                    """
                                    }
                                }
                                stage ('build integration runtime') {
                                    container('maven') {
                                        sh """
                                    cd runtime
                                    mvn -B -U clean install
                                    """
                                    }
                                }
                                stage ('build rest') {
                                    container('maven') {
                                        sh """
                                    cd rest
                                    mvn -B -U clean install fabric8:build -Pci
                                    """
                                    }
                                }
                                stage ('build ui') {
                                    container('yarn') {
                                        sh """
                                    cd ui
                                    yarn
                                    yarn ng build -- --aot --prod --progress=false
                                    """
                                    }
                                    container ('openshift') {
                                        sh """
                                    cd ui
                                    BC_DETAILS=`oc get bc | grep syndesis-ui || echo ""`
                                    if [ -z "\$BC_DETAILS" ]; then
                                        cat docker/Dockerfile | oc new-build --dockerfile=- --to=syndesis/syndesis-ui:latest --strategy=docker
                                    fi
                                    tar -cvf archive.tar dist docker
                                    oc start-build -F --from-archive=archive.tar syndesis-ui
                                    rm archive.tar
                                    """
                                    }
                                }

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
