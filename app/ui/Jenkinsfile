node {
  ws {
    checkout scm
    withMaven(
      maven: 'Maven 3.3.9',

      // Run the maven build
      sh "mvn clean install"
    }
  }
}
