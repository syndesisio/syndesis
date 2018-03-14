# The Platform Domain

The `Platform` domain contains application-level structures that are meant to govern the way Syndesis operates. This domain is a single-entry point for all things that are used across the application, regardless of what functionality the components or pieces of the UI that import them aim to accomplish.

## What can you find here and what you won't

In this domain you will find application-level types and models, Angular service definitions and custom TypeScript decorators, conveniently saved in different folders to help find them more easily. All the classes stored in the platform context are exposed through a barrel hierarchy to easily import those from anywhere in the application, like this:

```typescript
import { StringMap } from '@syndesis/ui/platform';
```

Some of the tokens available are custom TypeScript types, designed to conveniently wrap recurring schema patterns. Some others are TypeScript interfaces that model Syndesis entities that are meant to be used throughout the application and therefore are not bound to any piece of functionality or the UI feature module in particular.

Last but not least, we will want to define each entity's related service providers here, inside its corresponding folder along with its model file.

**What should you never save in this folder?** Any UI-related class or type which is meant to be used specifically within a feature module. In summary:

- Angular modules
- Components classes
- Route classes
- Pipes
- Service implementations
- HTML views
- SASS files
- Feature models and types

### Angular Service as Abstract Classes

As a rule of thumb, we will define service providers as abstract classes, implementing them later on in the `CoreModule` (usually following the same naming convention, or appending `ProviderService` instead of `Service` to the class name).

The reason for this is to ensure that services are declared just once in the injector, no matter where they are imported from. This prevents circular references and allows you to override provider implementations--if required--for varying factors, such as the environment, device, etc.

**NOTE**: Developers will want to import the token from Platform and not from the implementation made at `CoreModule`. For instance:

```typescript
// Wrong
import { UserProviderService } from '@syndesis/ui/core';

// Right
import { UserService } from '@syndesis/ui/platform';
```

### Provider Service Implementation

Please refer to `CoreModule` (currently in progress).

## State Management in Syndesis

Syndesis is currently being redesigned to become a state-driven application embracing an architectural pattern commonly known as REDUX. The rationale for this architectural choice is based on the fact that the product's business logic gravitates around several core entities.

Each of these core entities need to be coupled together to add value to the user interaction, yet also feature a high level of independence as standalone types, namely _Connections_, _Integrations_, _Connectors_ or _Extensions_, just to name the more relevant ones.

These entities interact with each other and force state changes amongst them. Ideally, all types are meant to be immutable--and components or service providers should never mutate or change these entities state.

As Syndesis grows into hundreds of components and dozens of providers, allowing the core entities to perform state changes will turn debugging undesired state changes and fixing bugs into an impossible mission.

The REDUX architecture prevents this from happening by forcing immutability in all platform objects and detaching state management from any piece of the UI. Basically, the UI endpoints (components, impure pipes, services) will subscribe to slices, or fragments, of state and will receive immutable objects through an Observable stream, rendering such information in their views, for instance.

![redux](https://user-images.githubusercontent.com/1104146/35936058-76933682-0c42-11e8-91e2-38ab6f025d61.png)


Whenever a change in state is required for whatever reason (either as the by-product of a user interaction or an event raised in some other end of the application), the UI endpoint involved will not want to alter the properties of such state snapshot, but will dispatch an action instead (picking an existing typed action from a pool of pre-defined applications actions). This action will sometimes include data in its payload, and other times it won't.

The aforementioned actions will be intercepted by _Reducers_. The `Platform` context features several reducer classes, one for each type/context that features its own slice of top-level state.

> **Fractal state in Syndesis Feature Modules** <br> In addition to the above, some lazy-loaded feature models (eg. Custom API Connection create wizard) also contain their own actions/reducers combo, as a sort of fractal implementation, handling state in an isolated fashion for that feature only.

These _Reducer_ file simply inspects a property available in all _Action_ objects named `type`. It then informs of the particular action request dispatched by the UI. The reducer operates as a basic `switch/case` flow. When a `case` matching the action `type` is found, the current state is cloned, changes are applied to this clone to fulfill the purpose of the action and then the clone is returned to replace the previously existing state snapshot.

If the action issued also features a _payload_ property, this will be fetched to help create the new state instance accordingly. Please bear in mind that both _Actions_ and _Reducers_ are meant to be [_Pure_ functions](https://hackernoon.com/functional-programming-concepts-pure-functions-cafa2983f757). This means that they conduct deterministic and, thus, predictable updates in the state slices of the Syndesis store. Therefore, **actions or reducers are not meant to trigger or depend on Http requests**.

Last but not least, there is also a business logic companion layer that _listens_ to fulfilled actions (that is, actions which have been achieved already), which can also dispatch new actions as the by-product of the former. These are called  _Effects_, and can usually be identified by the `.effects.ts` file suffix. These structures feature two major goals in Syndesis:

1. Subsequently trigger additional actions that are somewhat relevant to the original action, either consuming its already existing _payload_ or not. Eg: _Refresh integration metrics after conducting a state update on one particular Integration_.
2. Run (usually async) processes that require non-Redux logic, such as performing an HTTP call (usually wrapped by a Syndesis service provider method) to the REST API.

The latter is the most common case scenario, although the former is quite prevalent as well. The purpose for the latter is to load information through AJAX.

> **A common example: Fetching Integrations** <br>  Let's figure out we want to fetch Integrations from the REST API. A `FETCH_INTEGRATIONS` action will be dispatched, setting the `IntegrationState` slice of the store to `onLoading: true` mode. An effect configured to listen to the `FETCH_INTEGRATIONS` action will be fired up once this action is accomplished. Internally, it will submit an HTTP request to the REST API (via an injected instance of our `ApiHttpService` client, also available from the `Platform` domain).
>
>Once the HTTP client returns the API response, it will use it as a payload for a new `FETCH_INTEGRATIONS_COMPLETE` action, which will then update the state accordingly. This is most likely accomplished by resetting the `onLoading` property back to `false` and by populating some internal `collection` property with the response obtained from the API.
>
>Should an HTTP error occurs, such effect can then fire up a `FETCH_INTEGRATIONS_FAIL` action instead, resetting the state accordingly. This prevents the application from becoming unresponsive so that we still can give feedback to the end user.

## Actions, Reducers, and Effects in Syndesis

We will now put some basic examples of how to build our own slice of state within the Platform state store. We will use a very simple example where we will play around with a slice of state named `MetadataState`, which contains some basic application data: The application name ("_Syndesis_") and the application locale data (`en-gb`);

For your convenience, this example already exists in our codebase.

### Creating the State class

We will want to create an interface that implements the `BaseReducerModel`, since it will expose additional properties we might need to leverage, mostly for displaying errors or informing the UI that our model is syncing with the backend.

```typescript
import { BaseReducerModel } from '@syndesis/ui/platform';

export interface MetadataState extends BaseReducerModel {
  appName?: string;
  locale?: string;
}
```

### Creating Actions
Actions are required to populate the Metadata upon bootstrapping the application:

```typescript
import { Action } from '@ngrx/store';
import { MetadataState } from './metadata.models';

export const UPDATE = '[Metadata] General state update';
export const RESET     = '[Metadata] State reset to initial values';

export class MetadataUpdate implements Action {
  readonly type = UPDATE;

  constructor(public payload: MetadataState) { }
}

export class MetadataReset implements Action {
  readonly type = RESET;
}
```

With the above, we just need to dispatch the `UPDATE` action from within a component or service, and then we will obtain the `MetadataState` slice of the store updated desired. The `RESET` is quite self-explanatory, but we will see how we can take advantage of it later in our examples.

Let's see how to do it from the main root `app.component.ts`:

```typescript
import { Component, OnInit }             from '@angular/core';
import { Store }                         from '@ngrx/store';
import { PlatformState, MetadataUpdate } from '@syndesis/ui/platform';

@Component({
  selector: 'app-component',
  //... Template and StyleSheet URLs definition
})
export class AppComponent implements OnInit {
  constructor(private store: Store<PlatformState>) { }

  ngOnInit() {
    // We define the action payload
    const payload = {
      appName: 'Syndesis',
      locale: 'en-gb'
    };

    // We dispatch the desired action with the given payload
    this.store.dispatch(new MetadataUpdate(payload));
  }
}
```

The above will dispatch an `UPDATE` action, but no state change has been performed yet.

### Creating Our Own Reducer
Our reducer will initialize the state, meaning that it is already defined from the very moment the Store is initialized altogether. It will define action handlers that will respond to whatever action comes whose `type` property matches the `switch/case` defined:

```typescript
import { MetadataState } from './metadata.models';
import * as MetadataActions from './metadata.actions';

const initialState: MetadataState = {
  appName          : 'Syndesis',
  locale           : 'en-us',
  loading          : true,
  loaded           : false,
  hasErrors        : false,
  errors           : []
};

export function metadataReducer(state = initialState, action: any): MetadataState {
  switch (action.type) {
    case MetadataActions.UPDATE: {
      return {
        ...state,
        ...action.payload,
        loading: false,
        loaded: true
      };
    }

    case MetadataActions.RESET: {
      return initialState;
    }

    default: {
      return state;
    }
  }
}
```

We initialize the state in `loading` mode, and populate it as soon as the `UPDATE` action is fired up, which will populate the already existing state with the payload received and set the `loading` property to `false`.

With an interface modelling `MetadataState` and a reducer taking care of all the changes in its state, the only step left is to declare these two in order to make application aware of them.

Open `platform.reducer.ts` and add the interface definition to the platform state definition (the actual Store definition indeed). We then map a state token and the reducer to the `platformReducer` object, which maps state token names with its corresponding reducers:

```typescript
export interface PlatformState {
  metadataState: MetadataState;
  // More mappings will follow below...
}

export const platformReducer: ActionReducerMap<PlatformState> = {
  metadataState: metadataReducer,
  // More mappings will follow below...
};

```

### Dispatching actions depending on other actions and running middleware operations in the interim

The real magic behind actions relies on the fact that we can make actions respond to other actions. This allows for more complex business logic to be handled at a lower level, without necessarily depending on user interactions every time.

#### A real case scenario in Syndesis

A good example would be to have an _Fetch Connections_ action being triggered right after successfully processing a _Create Connection_ action, for argument's sake.

Initially a user will navigate to the Syndesis app, go to the Connections page, which will fetch all Connections in the Store that was populated upon bootstrapping the application (by means of an action), and then proceed to create a brand new Connection.

Once this new Connection is successfully created, the user might want to return to the Connections page and see the list updated with the newly created Connection. Since that list is only loaded when landing on the application for the first time, we need to re-trigger the _Fetch Connections_ action from the component we are at the end of the create connection process. However, that ties data management logic to components and makes the application less scalable. Moreover, if we ever want to understand which are the triggers of data changes in our application ecosystem, we'll want to track down each and every component.

Triggering actions from components is not wrong, but here we're facing a different scenario--we need to trigger an action that does not depend on a user interaction, but on the accomplishment of another action previously executed. Luckily, we can leverage _Effects_ to achieve that.

#### Effects in action with a code example

We saw earlier that our `MetadataState` example featured an `UPDATE` action that populated the overall state of this entity. New actions will be introduced as time goes by, and at some point we might need actions to report errors. An example would be:

```typescript
// rest of metadata.actioons.ts removed for brevity sake
export const REPORT_ERROR     = '[Metadata] Error reported';

export class MetadataReportError implements Action {
  readonly type = REPORT_ERROR;

  constructor(public error: Error) { }
}
// rest of metadata.actions.ts continues below...
```

Question: _What if we want to rollback the state of the metadata state to its original state whenever an error occurs?_ We can either dispatch the `MetadataReset` error along with each dispatching of the `MetadataReportError` action, or we can make the former depend on the latter. We can create a `metadata.effects.ts` field for that and populate it like this:

```typescript
import { Injectable } from '@angular/core';
import { Action } from '@ngrx/store';
import { Actions, Effect } from '@ngrx/effects';
import { Observable } from 'rxjs/Observable';

import * as MetadataActions from './metadata.actions';

@Injectable()
export class MetadataEffects {
  @Effect()
  resetMedatataUponError$: Observable<Action> = this.actions$
    .ofType(MetadataActions.ERROR)
    .map(() => ({ type: MetadataActions.RESET }));

  constructor(private actions$: Actions) { }
}
```

This is a simple example, but the most common use for _Effects_ is to allocate HTTP calls to the Syndesis REST API. In other words, and for the sole exception of simple, stateless components that does not impact the application or feature state, we **NEVER run HTTP calls (either directly or through Angular services) from components**. Instead, we proceed to call the API and digest its responses through Effects, like this:

```typescript
@Injectable()
export class MetadataEffects {
  // Other effects hidden for brevity sake
  // ...

  @Effect()
  fetchMetadataFromAPI$: Observable<Action> = this.actions$
    .ofType<MetadataActions.MetadataUpdate>(MetadataActions.UPDATE)
    .mergeMap(action =>
      this.metadataService
        .put(action.payload)
        .map(() => ({ type: MetadataActions.UPDATE_COMPLETE }))
        .catch(error => Observable.of({
          type: MetadataActions.ERROR,
          payload: error
        }))
    );

  constructor(
    private actions$: Actions,
    private metadataService: MetadataService // <-- New injected service
  ) { }
}
```

The example above involves the `UPDATE` action we already know, which is intercepted by the _Effect_, which will read its payload and send a `PUT` request to the API (by means of an imaginary `metadataService` object whose `put()` method leverages the `ApiHttpService` to send PUT requests).

When the HTTP call is successfully processed, the observable stream allows us to return another action (represented as a raw `{ type: MetadataActions.UPDATE_COMPLETE }` object literal, though we could use `new MetadataActions.UpdateComplete()` instead). We handle errors by also returning an `MetadataActions.ERROR` object literal.

In a nutshell, all action objects piped through the observable stream will be dispatched by the Effects handler once the observable stream reaches its end.

In the example provided, remember that the `ERROR` action also triggered a `RESET` action by means of the previously created `resetMedatataUponError$` effect class member.

### How to fetch observable state streams and subscribe to state changes
Let's assume that we have observed the guidelines above to create a slice of state in the platform store (namely `PlatformState`), which allows us to persist the existing Integrations. Or perhaps we might want to subscribe to changes in all the slices of state (which is not recommended anyway, given the volatility of the store). You could, then, do the following from any component:

```typescript
import { Component, OnInit } from '@angular/core';
import { Store }             from '@ngrx/store';
import { Observable }        from 'rxjs/Observable';
import {
  PlatformState,
  MetadataState,
  IntegrationState,
  selectIntegrationState }   from '@syndesis/ui/platform';

@Component({
  selector: 'app-stateful-element',
  //... Template and StyleSheet URLs definition
})
export class StatefulComponent implements OnInit {
  metadataState:     MetadataState;
  platformState$:    Observable<PlatformState>;
  integrationState$: Observable<IntegrationState>;

  constructor(private store: Store<PlatformState>) { }

  ngOnInit() {
    // Subscribes to the Metadata changes
    this.store
      .select<MetadataState>((state: PlatformState) => state.metadataState)
      .subscribe(metadataState => this.metadatState = metadataState)

    // Fetches the ENTIRE state as an Observable stream.
    this.platformState$ = this.store;

    // Fetches an Observable fragment of the state,
    // by means of a pre-defined custom selector.
    this.integrationState$ = this.store.select(selectIntegrationState);
  }
}
```

The example above leverages custom selectors to select partial slices of state. Please refer to the [online docs](https://github.com/ngrx/platform/blob/master/docs/store/selectors.md) about custom selectors for reference.

### Bear in mind these global state triggers
In `platform.actions.ts` you will find some top application-level actions that you can listen in your own effects should you need to run something when something happens:

- `APP_BOOTSTRAP`: This action is dispatched as soon as `AppComponent` is initialized in the Syndesis project.
