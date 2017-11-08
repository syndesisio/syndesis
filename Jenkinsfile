properties([
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
])

//We need a node so that we can have access to environemnt variables.
//The allocated node will actually be the Jenkins master (which is expected to provide these variables) as long as it has available executors.
node {

    def branch = "${env.BRANCH_NAME}"
    echo "Using branch: ${branch}."

    slave {
        withOpenshift {
            withMaven(
                envVars: [
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_ID', value: "${env.GITHUB_OAUTH_CLIENT_ID}"),
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_SECRET', value: "${env.GITHUB_OAUTH_CLIENT_SECRET}")
                ],
                serviceAccount: "jenkins", mavenSettingsXmlSecret: 'm2-settings', mavenLocalRepositoryPath: '/home/jenkins/mvnrepo/') {
                inside {
                    def testingNamespace = generateProjectName()

                    checkout scm

                    stage('Build') {
                        container(name: 'maven') {
                            sh "mvn -B -U clean install fabric8:build -Pci -Duser.home=/home/jenkins"
                        }
                    }

                    stage('System Tests') {
                        test(component: 'syndesis-rest', namespace: "${testingNamespace}", serviceAccount: 'jenkins')
                    }

                    if ("master" == branch) {
                        stage('Rollout') {
                            tag(sourceProject: 'syndesis-ci', imageStream: 'syndesis-rest')
                            rollout(deploymentConfig: 'syndesis-rest', namespace: 'syndesis-staging')
                        }
                    } else {
                        echo "Branch: ${branch} is not master. Skipping rollout"
                    }
                }
            }
        }
    }
}
