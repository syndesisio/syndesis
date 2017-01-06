#!/usr/bin/groovy
@Library('github.com/fabric8io/fabric8-pipeline-library@master')
def test = 'dummy'
mavenNode {
  dockerNode {
    ws{
      checkout scm
      sh "git remote set-url origin git@github.com:funktionio/funktion-connectors.git"

      // lets purge old releases
      def purgeFolder = "/root/.mvnrepo/io/fabric8/funktion/connector"
      sh """
echo file count in connectors mvn repo:
find ${purgeFolder} -print | wc -l
rm -rf ${purgeFolder}
echo file count after purge:
find ${purgeFolder} -print | wc -l
"""

      def pipeline = load 'release.groovy'
      def dockerImages = load 'releaseImages.groovy'

      def promoteImages = dockerImages.imagesBuiltByPipeline()

      echo "will create these docker images: ${promoteImages}"

      stage 'Stage'
      def stagedProject = pipeline.stage()

      // stage 'Approve'
      // pipeline.approveRelease(stagedProject)

      stage 'Promote'
      pipeline.release(stagedProject, promoteImages)

      stage 'Push Update Dependencies'
      def newVersion = stagedProject[1]
      pipeline.pushDependencyUpdates(newVersion)

      sh """
echo purging local maven repo:
rm -rf ${purgeFolder}
"""
    }
  }
}
