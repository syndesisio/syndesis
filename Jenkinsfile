#!/usr/bin/groovy
node{
  ws{
    checkout scm
    sh "git remote set-url origin git@github.com:fabric8io/funktion.git"

    def pipeline = load 'release.groovy'

    stage 'Stage'
    def stagedProject = pipeline.stage()

    // stage 'Approve'
    // pipeline.approveRelease(stagedProject)

    stage 'Promote'
    pipeline.release(stagedProject)

    stage 'Push Update Dependencies'
    def newVersion = stagedProject[1]
    pushDependencyUpdates(newVersion)
  }
}
