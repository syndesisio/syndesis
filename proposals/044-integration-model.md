
## Integration Model

* Issue: https://github.com/syndesisio/syndesis-project/issues/22
* Sprint: 14
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

## Objective

Currently integrations that are deployed are completely disconnected from the Syndesis DB afterwards. There is no way yet to update a running integration nor to see the state of the currently running integration.

The purpose of this document is to suggest a concise state model for `Integration` as well as define the interaction between the integrations created in  _syndesis-ui_ with the integrations running in the OpenShift which are created by a deployment operation.

When rethinking the integration and the state model, the following points are considered:

* Multiple `IntegrationRevisions` for an `Integration`
* Exactly one of these IntegrationRevisions can be active.
* Users can switch `IntegrationRevisions` versions from the UI
* `IntegrationRevisions` can be inspected from a history page.
* Old `IntegrationRevisions` can't be changed but can be used as a blueprint for new `IntegrationRevisions`.
* As soon as an integration is changed, a new `IntegrationRevision` is created. All changes go to this new revision until it is deployed by the user. After this, the revision gets a fixed version and cannot be changed anymore (i.e. its immutable)
* The user must always be able to see and examine the currently active `IntegrationRevision` along with its version number.

## IntegrationRevision state model

An `Integration` is the domain object describing Camel Routes used for performing integration tasks. 
Each `Integration` has properties which can be changed by a user from the UI like its name, so its mutable.

The actual logic like the connectors used and the configuration is encapsulated in an `IntegrationRevision`. 
Multiple `IntegrationRevision` can be connected to a single `Integration`.
Each `IntegrationRevision` represents a certain version of the defined integration and can be in different states.

Both objects are managed by `syndesis-rest` and `syndesis-ui`. 

When a user creates an integration, an overall `Integration` object is created as well as an `IntegrationRevision` which starts its life in the state _Draft_. 
A revision in this state has no runtime artefact attached and can be freely changed and saved by an user to the database. 
This is typically done automatically during the UI process of creating an integration.
Changing a draft revision is a cheap operation.

After a "Publish" UI operation the current revision is then transformed into a runnable artefact. 
This runnable artefact consists of a GitHub repo containing the code and configuration plus an OpenShift resource configuration which transforms this code into running application pods.
Running integration revisions can be suspended and resumed without destroying the deployment.

Once an integration revision is published and transfered to the state _Active_ it becomes **immutable** and can not be changed afterwards (except for state related properties).

`IntegrationRevisions` can be in one of the following state:

![Integration state model](images/integration-state.png "Integration State Model")

| State       | Description |
| ----------- |------------ |
| Draft | Initial state of an integration. The `IntegrationRevision` is not yet deployed |
| Active | `IntegrationRevision` is deployed and running |
| Inactive | `IntegrationRevision` is deployed but is not running|
| Undeployed | An `IntegrationRevision` has been undeployed | 
| Error | The `IntegrationRevision` is deployed but in an error state |

### Draft

Right after an integration revision is created it enters the _Draft_ state. This means 
the revision is not yet deployed. A revision leaves the draft state either by being published or deleted (in which case the whole integration revision is removed)

After an activation there is **no way back** to the _Draft_ state.

### Active

An _active_ integration revision is a running integration. This state can be either created from a _Draft_ state when the revision gets published or from an _Inactive_ state when a suspended integration revision is started again.

### Inactive

An _inactive_ intergration revision is a revision which was previously active and has been suspended by some operation. An _inactive_ revision can be resumed or undeployed.

### Undeployed

An integration revision which was published is never deleted but can be undeployed. An undeployed revision can be re-deployed (and activated) if the user chose the revision to be reactivated.

### Error

The error state indicates that the integration revision is not running. This state can no be entered via UI but only as a side effect in the backends. 
An integration revision can enter the error state any time. 

## State reconciliation 

Integrations follow a declarative reconciliation paradigm. 
An integration revision is always in a _currentState_. 
As consequence of an operation (see below) a new _targetState_ can be set. 
It is now the duty of the Syndesis backend to reconcile the _currentState_ to become the _targetState_.
This is modelled closely after the Kubernetes state model itself.

Reconciliation itself can be in one of two states:

* _Pending_ when `currentState != targetState`
* _Ok_ when `currentState == targetState`

This state is modelled implicitely by comparing both state variables. 

`Error` can never be set to a `targetState` by the user, but could be set by the backend to mark an integration revision as erroneous. 
The error state is typically part of a `currentState`. 
The backend controllers try to get out of the error state by might give up at some point. 

Every state change (current or target) should be tracked as event in an event table for auditing purposes (todo: ask keith)

## Operations

The following operations change the `targetState`. The `targetState` could be updated by simply changing the `IntegrationRevision`'s field, but it is recommended to use a more semantic rich Rest API call for perfoming this actions (i.e. having specific endpoint which can be used by the UI to trigger the targetState change).

* **Publish** sets the target state to _Active_. This operation can be called from the states:
  - _Draft_ when the integration revision should be published for the first time
  - _Inactive_ when the integration revision has been stopped
  - _Error_ when an integration revision should be re-deployed in case of an error to trigger a retry

* **Suspend** sets the target state to _Inactive_. This operation should be only be possible if the `currentState` is _Active_.

* **Resume** is an operation which can be used when the `currentState` is _Inactive_ and the revision should be re-activated. This sets the `targetState` to _Active_

* **Undeploy** marks an integration revision's targetState as _Undeployed_. An integration should be _Inactive_ before the _Undelpoyed_ state can be reached.

* **Delete** is the operation which removes an integration revision in a _Draft_ state.  

The `syndesis-rest` backend in this case will simply validate the context (i.e. whether the operation is allowed), updates the `targetState` and the returns to `syndesis-ui`. 
A background controller detects the state change and performs the proper actions to reach the target state. 

## Versioning

Each integration has a unique name within a certain context (which currently is the whole installation). 
Multiple revisions of an integration can exist in form of `IntegrationRevision`.
A _active_ integration revisions is immutable.
So in order to update an integration a new `IntegrationRevision` needs to be created.
Changing the _active_ `IntegrationRevision` in the UI causes a new `IntegrationRevision` to be created, which has the following characteristics:
* Its cloned from the _active_ `IntegrationRevision`
* The state of the clone is set to _Draft_
* The `parentVersion` is set to the version of the active `IntegrationRevision` from which it is cloned from.
* The `version` is set to undefined.

Now the user is able to change anything on this revision until it gets published. 
When the revision is published the following should happen:

* The `version` is set to a new unique version for this integration
* The _active_ integration revision's `targetState` is set to `Undeployed`.
* As soon the _Active_ state has been reached (callback), this integration's `targetState` is set to `Active`. Within the same even the formerly active revision is set to `Undeployed`.

This process should ensure that only a single revision of an integration is active.

## Data model

The following data model only highlights the parts which are specific to the lifecycle model. It ommits any other global or revision specific properties.

The state itself is modelled with a simple enum:

```java
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

The `Integration` holds all global properties, which can be mutated (like the name or description);


```java
public class Integration {
    
    // Name of the integration
    String name;
    
    // List of integration revisions for different versions
    List<IntegrationRevision> revisions;
    
    // Get the currently deployed revision (active / inactive) or null
    IntegrationRevision getDeployedRevision();
    
    // Get the current integration instance in state draft
    IntegrationRevision getDraftRevision();
    
    // More global properties ....
}
```

```java
public class IntegrationRevision {
  
    // The integration this instance belongs to
    Integration integration;
  
    // Current state of this revision
    IntegrationState currentState;
    
    // Target state of the revision
    IntegrationState targetState;
    
    // The version of the integration revision which was the origin of this revision.
    // 0 if this is the first revision of an integration
    int parentVersion;
    
    // Version of this integration revision
    int version;
  
    // Message describing the currentState further (e.g. error message)
    String currentMessage;
    
    // Message which should become the currentMessage after reconciliation
    String targetMessage;
    
    // Whether this integration has converged to its target state
    boolean isPending() {
        return currentState != targetState;
    }
    
    // All other props specific for a version of an integration revision. 
    // These properties are immutable after the Active state has ben reached
    // for the first time
    // ....
}
```

## Use cases

The following section describe some of the common use cases and how they are implemented within this new state model.

## Deployment of an integration revision

Deployment happens when an integration revision with `currentState == Draft` is set to `targetState = Active`. 
This happens when you publish an integration.
Several actions will be performed by the controller who detects this transition:

* A GitHub repository will be created if this is the first revision of an integration (parentVersion == 0). 
* If it is an new revision for an existing integration (e.g. because a user changed something), a new Git branch is created from the old revision's branch. This branch is named after the version of the revision.
* The runtime files are created and committed to the Git repository
* If this is an update of an existing integration, a currently running or suspended revision (`currentState == Active or Inactive`) is set to `targetState = Undeployed` 
* A new OpenShift build is created which in turns triggers a redeployment.
* The OpenShift deployment triggers the reconciliation of the currentState for the new, just deployed, revision and the undeployed old revision.

> Question: It would be awesome if we could change the state for an _Active_ integration to _Undeployed_ by watching OpenShift for update deployments so that we can mark the old integration as _Undeployed_ and the new as _Active_. 
> Is this possible with OpenShift ? How to correlate OpenShift deployments with integration versions ?

### Updating a integration

When changing an integration the following steps and checks are performed. 

* If there is already an integration revision which is in state _Draft_, simply update this integration and save it to the DB.
* If there is no revision in _Draft_ but one in _Active_, the active integration is cloned to a new integration revision in state _Draft_. This integration then can be changed freely and saved to the DB (as above)

Deployment then works as described abov.

## UI changes and updates

Within the UI the state of an integration should be visualized. 

It is suggested that when the reconciliation state is `Ok` the _currentState_ should be shown and when the reconciliation state is `Pending` then "Pending" should be shown (possibly whith the `targetState` as a tool tip info). Alternatively, when a reconciliation process the label could be more specific about the action just performing ("Deploying", "Suspending", "Resuming", "Undeploying") which can be infered from `currentState` and `targetState`

| `currentState` | `targetState` | Label |
| -------------- | ------------- | ----- |
| Draft | Active | Deploying |
| Active | Inactive | Suspending |
| Inactive | Active | Resuming |
| Inactive | Undeployed | Undeploying |
