#!/usr/bin/groovy
def repo(){
 return 'fabric8io/funktion'
}

def stage(){


  return stageProject{
    project = repo()
    useGitTagForNextVersion = true
    setVersionExtraArgs = '-pl parent'
  }
}

def approveRelease(project){
  def releaseVersion = project[1]
  approve{
    room = null
    version = releaseVersion
    console = null
    environment = 'fabric8'
  }
}

def release(project){
  releaseProject{
    stagedProject = project
    useGitTagForNextVersion = true
    helmPush = false
    groupId = 'io.fabric8.funktion'
    githubOrganisation = 'fabric8io'
    artifactIdToWatchInCentral = 'funktion-runtime'
    artifactExtensionToWatchInCentral = 'jar'
  }
}

def mergePullRequest(prId){
  mergeAndWaitForPullRequest{
    project = repo()
    pullRequestId = prId
  }

}
return this;
