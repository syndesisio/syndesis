
## Integration Model

* Issue: https://github.com/syndesisio/syndesis-project/issues/22
* Sprint: 14
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

## Objective

Currently integrations that are deployed are completely disconnected from the Syndesis DB afterwards. There is no way yet to update a running integration nor to see the state of the currently running integration.

The purpose of this document is to suggest a concise state model for `Integrations` as well as define the interaction between the `Integration`s designed in _syndesis-ui_ with the integrations running in the OpenShift cluster whose creation is triggered by a deployment action by the user.

When rethinking the integration and the state model, the following points should be considered:

* Multiple versions of an Integration must be able to exist.
* Exactly one of these integrations can be active.
* Each deployment of an Integration creates a new version.
* Users can switch integration versions from the UI
* Versioned Integrations can be inspected from a history page.
* Old integrations can't be changed but can be used as a blueprint for new integrations.
* As soon as an integration is changed, a new version of an integration is created. All changes go to this new integration until it is deployed by the user. After this, its gets a fixed version and cannot be changed anymore.
* The user must always be able to see and examine the currently active integration along with its version number.

_Attention: THIS IS WORK-IN-PROGRESS. The propsoal is PRed early to collect early feedback_


## Integration state model

Technical an Integration is the domain object describing a Camel Route. 
It is managed by `syndesis-rest` and `syndesis-ui`. 
During design time this object only lives in this domain. 
After a "Publish" operation the integration is transformed in an runnable artefact. 
This runnable artefact consists of a GitHub repo containing the code and configuration and an OpenShift resource configuration which transforms this code into a running application, backed by an integration pod.
Running integrations can be suspended and resumed without destroying the deployment.


![Integration state model](images/integration-state.png "Integration State Model")

| State       | Description |
| ----------- |------------ |
| Draft | Initial state of an integration. The integration is not yet deployed |
| Active | Integration is deployed and running |
| Inactive | Integration is deployed but is not running|
| Undeployed | An integration has been undeployed | 
| Error | The integration is deployed but in an error state |

### Draft

Right after an integration is created it enters the _Draft_ state. This means 
the integration is not yet deployed. An integration leaves the draft state either by being published or deleted (in which case the whole integration is removed)

After an activation there is no way back to the _Draft_ state.

### Active

An _active_ integration is a running integration. This state can be either created from a _Draft_ state when the integration gets published of from an _Inactive_ state when a suspended integration is started again.

### Inactive

An _inactive_ integration is an integration which was previously active and has been suspended by some action. An _inactive_ integration can be resumed or undeployed

### Undeployed

An integration which was published is never deleted but can be undeployed. An undeployed integration can be re-deployed (and activated) if the user chose the version of this integration to be reactivated.

### Error

The error state indicates that the integration is not running. This state can be entered consciously but only as a side effect. An integration can enter the error state any time. 

## State reconciliation 

Integrations follow a declarative reconciliation paradigm. An integration is always in a _currentState_. As consequence of an _action_ (see below) a new _targeState_ can be set. It is now the duty of the Syndesis backend to reconcile the _currentState_ to become the _targetState_.
This modelled is closely to the Kubernetes state model itself.

Reconciliation itself can be in one of two states:

* _Pending_ when `currentState != targetState`
* _Ok_ when `currentState == targetState`

This state is modelled implicitely by looking at both state variables. 

`Error` can never be a `targetState` but it can be a `currentState`. The backend controllers try to get out of the error state but might give up at some point. 

Every state change (current or target) should be tracked. 

## Operations

The following operations change the `targetState`. The `targetState` could be updated by simply changing the Integration's field, but it is recommended to use a more semantic rich REST API call for perfoming this actions (i.e. having specific endpoint which can be used by the UI to trigger the targetState change).

* **Publish** sets the target state to _Active_. This operation can be called from the states:
  - _Draft_ when the integration should be published for the first time
  - _Inactive_ when the integration has been stopped
  - _Error_ when an integration should be re-deployed in case of an error to trigger a retry

* **Suspend** sets the target state to _Inactive_. This operation should be only be possible if the `currentState` is _Active_.

* **Resume** is an operation which can be used when the `currentState` is _Inactive_ and the integration should be re-activated. This sets the `targetState` to _Active_

* **Undeploy** marks an integration target state as _Undeployed_. An integration should be _Inactive_ before the _Undelpoyed_ state can be reached.

* **Delete** is the operation which removes an integration in an _Draft_ state.  

The `syndesis-rest` backend in this case will simply validate the context (i.e. whether the operation is allowed), updates the `targetState` and the returns to `syndesis-ui`. 
A background controller detects the state change and performs the proper actions to reach the target state. 

## Versioning

Each integration has a unique name within a certain context (which currently is the whole installation). 
Multiple versions of an integration can exist. 
A _active_ integration is immutable.
So in order to update an active integration (i.e. an integration which is in state _Active_) a new version needs to be created.
Changing an _active_ integration in the UI causes a new integration to be created, with the following characteristics:
* Its cloned from the _active_ integration
* The state of the clone is set to _Draft_
* The `parentVersion` is set to the version of the active integration it is cloned from
* The `version` is set to undefined.

Now the user is able to change anything on this integration until it gets published. When its published the following should happen:

* The `version` is set to a new unique version for this integration
* The _active_ integration's `targetState` is set to `Undeployed`
* As soon this state has been reached (callback), this integration's `targetState` is set to `Active`.

That way it is ensured that only a single version of an integration is active. 

## Data model

The following data model only highlights the parts which are specific to the lifecycle model:

```java
public class Integration {
    
    // Unique name of the integration
    String name;
    
    // Current state of the integration
    IntegrationState currentState;
    
    // Target state of the integration    
    IntegrationState targetState;
    
    // The version of the integration which was the origin of this integration
    // 0 if this is a new integration
    int parentVersion;
    
    // Version of this integratin
    int version;
  
    // Message describing the state further (e.g. error message)
    String stateMessage;
    
    // Whether this integration has converged to its target state
    boolean isPending() {
        return currentState != targetState;
    }
}

enum IntegrationState {
  
    // Initial state of an undeployed integration
    Draft,
  
    // Deployed and running
    Active, 
  
    // Deployed and suspended
    Inactive,
  
    // Undeployed (no associated pods)
    Undeployed,
  
    // Error occured
    Error;
}
```

## Deployment

Deployment goes when an integration with `currentState == Draft` set `targetState = Active`. 
Several actions will be performed by the controller:

* A GitHub repository will be created if this is the first version of an integration (parentVersion == 0). 
* If it is an new version of an existing integration (e.g. because a user changed something), a new Git branch is created from the old version's branch. This branch is named after the version.
* The runtime files are created and commit to the Git repository
* If this is an update of an existing integration, a currently running or suspended integration (`currentState == Active or Inactive`) is set to `targetState = Undeployed` (which triggers an undeployment on behalf of the controller)
* The resource descriptors are created for this integration and applied to OpenShift.

Open Question: For an update should an exisiting `DeploymentConfig` be reused (and e.g. doing a rolling update) or should we create a new `DeploymentConfig` ?

## UI changes and updates

Within the UI the state of an integration should be visualized. 
It is suggested that when the reconciliation state is `Ok` the _currentState_ should be shown and when the reconciliation state is `Pending` then "Pending" should be shown (possibly whith the `targetState` as a tool tip info). Alternatively, when a reconciliation process the label could be more specific about the action just performing ("Deploying", "Suspending", "Resuming", "Undeploying") which can be infered from `currentState` and `targetState`

| `currentState` | `targetState` | Label |
| -------------- | ------------- | ----- |
| Draft | Active | Deploying |
| Active | Inactive | Suspending |
| Inactive | Active | Resuming |
| Inactive | Undeployed | Undeploying |
