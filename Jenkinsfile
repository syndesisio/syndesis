pipeline {
    agent any
    tools {
        jdk 'JDK8'
        maven 'maven-3.3.9'
        nodejs 'node 8.9.1'
    }
    environment {
        PATH="${tool('oc')}:$PATH"
        KUBE_CONFIG="${WORKSPACE}/.kubr/config"
    }
    stages {
        stage('prepare') {
            steps {
             withCredentials([string(credentialsId: 'api.rh-idev.openshift.com', variable: 'TOKEN')]) {
                    sh """
                    oc login --server=https://api.rh-idev.openshift.com --token=$TOKEN
                    oc project syndesis-ci
                    """
                }
            }
        }
        stage('build') {
            steps {
                checkout scm
                sh """
                echo $PATH
                ./build.sh --image-mode s2i --batch-mode --mode system-test
                """
            }
        }
    }
    post {
        always {
            sh 'oc logout'
        }
    }
}
