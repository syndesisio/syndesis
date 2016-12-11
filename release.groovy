#!/usr/bin/groovy
def repo(){
 return 'fabric8io/funktion'
}


def stage(){
  return stageProject{
    project = repo()
    useGitTagForNextVersion = true
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

def release(project, promoteImages){
  releaseProject{
    stagedProject = project
    useGitTagForNextVersion = true
    helmPush = false
    groupId = 'io.fabric8.funktion'
    githubOrganisation = 'fabric8io'
    artifactIdToWatchInCentral = 'funktion-runtime'
    artifactExtensionToWatchInCentral = 'jar'
    imagesToPromoteToDockerHub = promoteImages
    promoteToDockerRegistry = 'docker.io'
    dockerOrganisation = 'fabric8'
  }
}

def mergePullRequest(prId){
  mergeAndWaitForPullRequest{
    project = repo()
    pullRequestId = prId
  }

}

def pushDependencyUpdates(newVersion){
  def parentPomProjects = ['fabric8-quickstarts/funktion-nodejs-example','fabric8-quickstarts/funktion-kotlin-example',
                           'fabric8-quickstarts/funktion-java-example','fabric8-quickstarts/funktion-groovy-example']
  pushParentPomVersionChangePR{
    projects = parentPomProjects
    version = newVersion
  }
}
return this;
