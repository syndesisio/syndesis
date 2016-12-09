#!/usr/bin/groovy
node{
  ws{
    checkout scm
    sh "git remote set-url origin git@github.com:fabric8io/funktion.git"

    def pipeline = load 'release.groovy'
    def dockerImages = load 'releaseImages.groovy'

    echo "will create these docker images imagesBuiltByPipeline(): ${imagesBuiltByPipeline()}"
    echo "will create these docker images dockerImages.imagesBuiltByPipeline(): ${dockerImages.imagesBuiltByPipeline()}"

    stage 'Stage'
    def stagedProject = pipeline.stage()

    // stage 'Approve'
    // pipeline.approveRelease(stagedProject)

    stage 'Promote'
    pipeline.release(stagedProject)

    stage 'Push Update Dependencies'
    def newVersion = stagedProject[1]
    pipeline.pushDependencyUpdates(newVersion)
  }
}
