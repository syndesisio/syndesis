[![Cypress.io tests](https://img.shields.io/badge/cypress.io-tests-green.svg?style=flat-square)](https://cypress.io)

# Syndesis UI

Syndesis UI is a single page application built with React.

[Storybook](https://syndesisio.github.io/syndesis-react/)

##### Table of Contents

- [Syndesis UI](#syndesis-ui)
        - [Table of Contents](#table-of-contents)
  - [This repository](#this-repository)
    - [syndesis](#syndesis)
    - [packages/api](#packagesapi)
    - [packages/models](#packagesmodels)
    - [packages/ui](#packagesui)
    - [packages/utils](#packagesutils)
    - [typings](#typings)
  - [Setup and preparation](#setup-and-preparation)
  - [Development workflow](#development-workflow)
    - [`yarn build`](#yarn-build)
    - [`yarn watch:app:minishift`](#yarn-watchappminishift)
    - [`yarn watch:app:minishift:restore`](#yarn-watchappminishiftrestore)
    - [`BACKEND=https://syndesis.192.168.64.1.nip.io yarn watch:app:proxy`](#backendhttpssyndesis192168641nipio-yarn-watchappproxy)
    - [`yarn watch:packages`](#yarn-watchpackages)
    - [`yarn test`](#yarn-test)
    - [`yarn storybook`](#yarn-storybook)
  - [How to](#how-to)
    - [Internationalization](#internationalization)
      - [Internationalizing a render method](#internationalizing-a-render-method)
      - [Internationalizing text in a constant](#internationalizing-text-in-a-constant)
      - [Translation Examples](#translation-examples)
        - [Simple translation using default namespace](#simple-translation-using-default-namespace)
        - [Translations using different namespaces](#translations-using-different-namespaces)
        - [Translation with arguments](#translation-with-arguments)
        - [Nested translation](#nested-translation)
        - [Translation as an argument to another translation](#translation-as-an-argument-to-another-translation)
        - [Adding plurals to a translation](#adding-plurals-to-a-translation)
    - [Routing](#routing)
      - [Introduction to routing](#introduction-to-routing)
      - [App's routes and resolvers](#apps-routes-and-resolvers)
      - [Routes and app state (i.e. passing data between routes)](#routes-and-app-state-ie-passing-data-between-routes)
        - [A simple example](#a-simple-example)
        - [URLs as a single source of truth](#urls-as-a-single-source-of-truth)
        - [Where we are going there ~~are no roads~~ is no app state](#where-we-are-going-there-are-no-roads-is-no-app-state)
    - [Testing](#testing)
      - [Adding test identifiers](#adding-test-identifiers)
      - [Unit testing](#unit-testing)
      - [Integration testing](#integration-testing)
  - [License](#license)

## This repository 

Code is split in many packages, organized as a monorepo using [Yarn's workspaces](https://yarnpkg.com/lang/en/docs/workspaces/).
The workspace is configured like this:

```
"syndesis",
"packages/*"
"typings/*"
```

We use [Lerna](https://github.com/lerna/lerna) to streamline the development process; the most common operations, such as building the project or running the development mode can be done directly from the project root.  
Check the [development workflow](#development-workflow) section for a list of available commands.

### syndesis

`syndesis` is the main application. It handles the authentication against Syndesis's OAuth Server, and provides the main
 app layout where "sub-apps" can be injected.

It's built with [create-react-app](https://github.com/facebook/create-react-app).

### packages/api

This package contains a collection of React Components implementing the [render props pattern](https://reactjs.org/docs/render-props.html)
to ease interacting with Syndesis's Backend.


### packages/models

This package contains the TypeScript definitions of the models, as read from the backend.

### packages/ui

This package contains a collection of UI elements that are common across the application. 

All the elements are written as React PureComponents or Stateless Functional Components. The idea is to decouple the 
presentation from the model that holds the data that needs to be presented to promote code reuse and easing the testing
efforts. 

### packages/utils

This package contains commonly used components of function that don't fit any of the above packages.

### typings

Extra typings for pure JavaScript dependencies that should eventually be pushed on [DefinitelyTyped](https://github.com/DefinitelyTyped/DefinitelyTyped/).

## Setup and preparation 

[Yarn](https://yarnpkg.com) is the package manager required to work on the project.


```bash
# install all the dependencies: 
yarn install

# build the project
yarn build
```

## Development workflow

### `yarn build`

Builds the project for production.

### `yarn watch:app:minishift` 

Runs the app in the development mode. This should be used within [Syndesis local development environment](https://doc.syndesis.io/#dev-local).    

Open the Syndesis UI in your browser from the command line by running:
```bash
# on macOS
$ open https://$(oc get routes syndesis --template "{{.spec.host}}")

# on linux
$ xdg-open https://$(oc get routes syndesis --template "{{.spec.host}}")

# on windows
$ start https://$(oc get routes syndesis --template "{{.spec.host}}")
```

**IMPORTANT**

* the app will also be available at [http://localhost:3000](http://localhost:3000), but the communication with the backend will not work this way. You should open the UI as stated above.   
* you must have successfully built all the packages _before_ running the watch command to successfully 
run this command. Changes to sources outside `syndesis` folder will not be detected; run the appropriate development mode in parallel with this if you need this.
* this will change the `syndesis-ui` POD to point to your development server. To 
restore the POD to the original state, you will have to manually run `yarn watch:app:minishift:restore`.

### `yarn watch:app:minishift:restore`

Restores the `syndesis-ui` POD to the original state. 

### `BACKEND=https://syndesis.192.168.64.1.nip.io yarn watch:app:proxy`

Runs the app in the development mode. API calls will be proxied to the provided `BACKEND` URL.

The proxy will require the session cookies to be able to properly work. The right cookies will be retrieved through an automated instance of Chromium, that will wait for the user to login. Once properly logged in - a condition recognized by the browser navigating back to the provided `BACKEND` url - the session cookies will be extracted and the Chromium instance will be closed.

It's also possibile to enable the _chaos mode_ setting the `CHAOS` env variable. In _chaos mode_ API requests have a 50% chance to fail with an error 500.   
This mode is useful to test the resilience of the app.  

**IMPORTANT**

* the `BACKEND` URL should be the project URL as retrieved by Minishift/Openshift console.
* doublecheck that the URL doesn't end with a '/'.
* never close the automated Chromium browser manually; if it stays open probably after a successful login something went wrong and should be debugged.

### `yarn watch:packages` 

Watches 'packages' source files and rebuilds on any change.

To watch a specific package you can pass the package name:

```bash
$ yarn watch:packages --scope @syndesis/package-name
```

### `yarn test`

Runs the test suite.

To run the test suite for a specific package you can pass the package name:

```bash
$ yarn test --scope @syndesis/package-name
```

### `yarn storybook`

Some packages provides a [Storybook](https://storybook.js.org) to develop and document the components in isolation. This will launch all the available storybooks.   

A browser tab per each storybook enabled package will be automatically opened.

To open a specific storybook you can pass the package name:

```bash
$ yarn storybook --scope @syndesis/package-name
```

## How to

### Internationalization

We are using the [react-i18next library](https://github.com/i18next/react-i18next) for internationalization (i18n). You can find documentation about this library at [react.i18next.com](https://react.i18next.com).

The [syndesis package](#architecture) is the **only** package where we are using this *i18n* library. To make this *i18n* library available in the `syndesis` package, the `syndesis/package.json` file was edited as follows:
- added dependency to [i18next](https://github.com/i18next/i18next) - the core library that handles all the translation functionality.
- added a dependency to [i18next-browser-languagedetector](https://github.com/i18next/i18next-browser-languageDetector) - detects the browser's locale and sets it as the default locale for translations.
- added dependency to [react-i18next](https://github.com/i18next/react-i18next) - built on top of the core library and provides functionality specific to *React*.
- added `@types/i18next` to `devDependencies`.
- added `@types/i18next-browser-languagedetector` to `devDependencies`.

If [packages](#architecture) other than the `syndesis` package require translation, the already translated text should be passed into those packages using the custom properties of the component. **Do not modify the packages' `package.json` to add dependencies to the *i18n* framework!** We are doing it this way to facilitate testing and to remove the impact of changing the *i18n* library, should we ever need to do that. Here is an example of how you would do that:

```typescript
export interface ILinkProps {
  linkRoute: string;
  i18LinkText: string;
}

export class MyLink extends React.Component<ILinkProps> {
  public render() {
    return (
      <Link
        to={this.props.linkRoute}
        className={'btn btn-primary'}
      >
        {this.props.i18LinkText}
      </Link>
    );
  }
}
```

In order to get internalization working in our app, a specific [i18next instance](https://react.i18next.com/latest/i18next-instance) needs to be configured. We do this in the `syndesis/src/i18n` folder. Here is how that folder is organized:
- `index.ts` - configures our *i18next instance*. You can find more information about `i18next` settings at [i18next.com](https://www.i18next.com/overview/configuration-options) and the `react-i18n` additional settings at [react.i18next.com](https://react.i18next.com/latest/i18next-instance).
- `locales/` - a folder that sets up the *i18n* namespaces and contains the translations shared within the `syndesis/src` codebase.
- `locales/index.ts` - contains references to all the app translation files.
- `locales/shared-translations.en.json` - contains the shared English translations and placeholders for the namespaces provided from within `syndesis/src`.
- `locales/shared-translations.it.json` - contains the shared Italian translations and placeholders for the namespaces provided from within `syndesis/src`.

Now that we have our *i18n instance* configured, we can start *i18n*-ing our code. This is done by adding a `locales/` folder at the root of the code that you want to have its own, non-shared translations. This folder defines your translations and your namespace. Now, all that is left to do is to add specific calls to the *i18n* framework from you code.

**Important**: Adding new namespaces does require changes to be made to the *i18n instance* described above.

**Note**: All namespaces are actually always available by qualfiying the translation key with the namespace.

When a method returns a *React* element or fragment (like `render()` does), and translations are needed, the [NamespacesConsumer](https://react.i18next.com/legacy-v9/namespacesconsumer) class is used. Using the `NamespacesConsumer` class in the method gives that method access to the *i18n* framework. It does this by exposing the `t`, or translation function. An array of namespaces, which are setup by your *i18n instance*, is used by the `NamespacesConsumer` to perform the translations. The first namespace in the array **does not** require its keys to be qualified. However, any subsequent namespaces **do** require their keys to be qualified. An example of internationalizing a method like this can be found [here](#internationalizing-a-render-method).

When translations are needed but you are not in a method that returns a *React* element, or you are in code that is not a method at all, the `NamespacesConsumer` cannot be used. Instead, the *i18n instance* itself is used. For instance, you can translate some text in a `const` that is constructing an instance of an `interface`. See [this](#internationalizing-text-in-a-constant) example.

The following sections give you examples on how to *i18n* things.

#### Internationalizing a render method

To internationalize a `render()` method, do the following:

1. Add this import statement
```typescript
import { NamespacesConsumer } from 'react-i18next';
```
1. Wrap what normally would be returned with a `NamespacesConsumer` tag. You need to set the array of namespaces you will be using. The first namespace is the default and does not need to be used when referencing a translation key. Translation keys not found in the default namespace need to be qualified. See [this](#translations-using-different-namespaces) for examples of using namespaces in translations.
```typescript
public render() {
  return (
    <NamespacesConsumer ns={['your-default-namespace', 'additional-namespaces']}>
      {t => (
        // include the code that you would normally return
      )}
    </NamespacesConsumer>
  );
}
```
3. Add translations to your translation files.
4. Use the `t` function in the `render()` method wherever a translation is needed. [Here](#translation-examples) you will find examples of how to use the `t` function.

#### Internationalizing text in a constant

To internationalize text in a `const`, do the following:

1. Add this import statement:
```typescript
import i18n from 'relative-path-to-syndesis/src/i18n';
```
2. Add translations into your translation files.
3. Use the `i18n.t` function to perform the translations. [Here](#translation-examples) you will find examples of how to use the `t` function.
```typescript
const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;
```

#### Translation Examples

Some examples taken from the [i18next.com](https://www.i18next.com/translation-function/nesting) documentation.

##### Simple translation using default namespace
- translation file
```json
{
  "errorMsg": "An error occurred.",
}
```
- usage
```typescript
{ t('erroMsg') }
```
- outputs: "An error occurred."

##### Translations using different namespaces
- usage
```typescript
{ t('Connections') } -> uses default namespace translation files
{ t('shared:Name') } -> uses 'shared' namespace translation files
{ t('integrations:topIntegrations') } -> uses 'integrations' namespace translation files
```

##### Translation with arguments
- translation file
```json
{
  "lastNumberOfDays": "Last {{numberOfDays}} Days",
  "favorite": "{{this}} is my favorite {{thing}}",
}
```
- usage
```typescript
{ t('lastNumberOfDays', { numberOfDays: 30 }) }
{ t('favorite', { this: 'Apple', thing: 'fruit' }) }
```
- outputs: "Last 30 Days" and "Apple is my favorite fruit"

##### Nested translation
- translation file
```json
{
    "nesting1": "1 $t(nesting2)",
    "nesting2": "2 $t(nesting3)",
    "nesting3": "3",
}
```
- usage
```typescript
{ t('nesting1') }
```
- outputs: "1 2 3"

##### Translation as an argument to another translation
- translation file
```json
{
      "key1": "hello world",
      "key2": "say: {{val}}"
}
```
- usage
```typescript
{ t('key2', { val: '$t(key1)' }) }
```
- outputs: "say: hello world"

##### Adding plurals to a translation
- translation file
```json
{
  "numberOfItems": "{{count}} item",
  "numberOfItems_plural": "{{count}} items",
}
```

### Routing

#### Introduction to routing

Routing is handled by the [React Router](https://github.com/ReactTraining/react-router) library.

A route is a string representing a URL (eg. `/foo/bar`), and it almost always has a companion component that represents the page we want rendered when the browser navigates to that URL. In the codebase these components are called _Pages_.

```tsx
<Route match={'/foo/bar'} component={FooBarPage} />
```

In this example, when the browser navigates to the `/foo/bar` route the `Route` component will render the `FooBarPage` component.

To link to a specific route the `Link` component should be used.

```tsx
<Link to={'/foo/bar'}>Take me to FooBar</Link>
```

Why a `Link` and not a simple `a`? The `Link` component uses the `history` API to manipulate the navigation history, and the underlying `Router` component listens to changes to the history to trigger the right re-render of the app. This way the app is able to move between pages without the classic "reload" effect of normal websites (AKA [Single Page Application](https://en.wikipedia.org/wiki/Single-page_application)).

Since a URL is used in many places across the app, it's suggested to use the `named-urls` library to give it a unique name, avoid repetitions, and remove the risk of typos.
Unfortunately, this is not a feature that `react-router` provides out of the box, so we use [named-urls](https://github.com/tricoder42/named-urls) to achieve that.  With this library, the previous examples would, instead, look like this:

```ts
// routes.ts
import { include } from 'named-urls';

export default {
 foo: include('/foo', {
   bar: '/bar'
 })
};
```

```tsx
import routes from './routes'

<Route match={routes.foo.bar} component={FooBarPage} />
```

```tsx
import routes from './routes';

<Link to={routes.foo.bar}>Take me to FooBar</Link>
```

A route can also accept dynamic parameters. Dynamic parameters are written in the URL string, and their presence can be set as either mandatory or optional.

```tsx
<Route match={'/bar/:mandatoryParameter/:optionalParameter?'} component={FooBarPage} />

<Link to={`/foo/bar/${someVar}/${someOtherVar}`}>Take me to FooBar</Link>
```

In this example the `foo.bar` route requires a parameter called `mandatoryParameter` and can take an optional parameter called `optionalParameter`.
The `Link` component doesn't offer any specific helper to generate the right URL for you, so the right string needs to be programmatically generated depending on your needs.

This approach is extremely error-prone and repetitive. It's better to have some helper function that, provided the right parameters, resolves the URL for you. In the codebase these helpers are called `reducers`.

```ts
// routes.ts
import { include } from 'named-urls';

export default {
 foo: include('/foo', {
   bar: '/bar/:mandatoryParameter/:optionalParameter?'
 })
};
```

```ts
// reducers.ts
import { reverse } from 'named-urls';
import routes from './routes';

export default {
 foo: {
   bar: (params: { mandatoryParameter: string, optionalParameter: number }) =>
     reverse(routes.foo.bar, params)
 }
};
```

```tsx
import resolvers from './resolvers';

<Link to={resolvers.foo.bar({ mandatoryParameter: 'hello' })}>Take me to FooBar</Link>
```

This approach brings a number of benefits:

- function parameters are easier to read than a string
- refactoring a function is easier than searching across the app for the url string
- a parameter could be a complex object, enabling the possibility to derive the right value to pass to the URL keeping this logic DRY

Downsides are:

- it's very verbose
- if the URL changes and the resolver isn't updated accordingly, the resolver will not work. *As such, unit testing resolvers is very important.*

#### App's routes and resolvers

Before anything else, let's talk about _modules_. A module is a subsection into which the app is logically split (eg. integrations, customizations, ...) and its main use is to keep the code organized by concern. Modules can be found in the [syndesis/src/modules](syndesis/src/modules) folder.

Each module will take care of its own routes and pages, plus anything else that's required for the module to properly work and should export:

* its _named_ routes definitions in a `routes.ts` file
* its resolvers in a `resolvers.ts` file

These two files will then be re-exported by the main app [routes](syndesis/src/modules/routes.ts) and [resolvers](syndesis/src/modules/resolvers.ts) files, to somewhat reduce the coupling between modules.

#### Routes and app state (i.e. passing data between routes)

Hard rule: a page must be able to render *regardless of the navigation history* that lead the user to it.\*
_\* exceptions can be made for pages that act like an actual application, such as a wizard._

##### A simple example  

Consider this scenario:

* our app has two routes
  * `/integrations`, that will render the `IntegrationsListPage` component
  * `/integrations/detail`, that will render the `IntegrationDetailsPage` component
* a user is checking the integrations list page (`/integrations`)
* the user then clicks on the "Details" link next to the integration named `Foo`, changing the url to `/integrations/detail`
* the `IntegrationDetailsPage` component is then rendered, but...

...how can the `IntegrationDetailsPage` know that the `Foo` object should be rendered? It has not been "passed" to it yet!

One option would be to make the app stateful storing the object that the user clicked, allowing the details page to retrieve it and properly render. This can be achieved using [React's Context](https://reactjs.org/docs/context.html), or a state management solution like [redux](https://redux.js.org/).

##### URLs as a single source of truth

Let's assume that we have the object in the app state and the `IntegrationDetailsPage` is properly rendering. Everything is good and the problem appears to be solved, but what if the user decides to share the URL - which is `/integrations/detail` - with a colleague?

* the user shares the `Foo` details page url with a colleague
* the colleague clicks the link, and its browser promptly opens it
* the `IntegrationDetailsPage` assumes that the object to render is available in the app's state, but the object is actually `undefined` and the app crashes with an error

Since our app relies on the user "correctly" following the app's flow to set its state, it doesn't work when the flow is broken. A band-aid could be put in the code to check for the object validity before accessing it, showing some meaningful error message to the user and avoiding a crash, but this wouldn't be the greatest user experience ever.

A better solution would be instead to tell the `IntegrationDetailsPage` enough information about the object that should be rendered, to allow the page to work independently from the rest of the app.

In this case, it would be as simple as passing the object ID in the URL - which would look something like `/integrations/123-some-id/detail` - and update the page to read the ID from the URL, to then fetch the required data from an API.
The colleague's experience would now be something like this:

* the user shares the `Foo` details page URL (`/integrations/123-some-id/detail`) with a colleague
* the colleague clicks the link, and the browser promptly opens it
* the `IntegrationDetailsPage` checks the app state, but no object is there ready to be displayed. It then reads the ID `123-some-id` from the URL and calls an API to retrieve the object from the database
* in the meantime, some loading indicator is shown to the user to inform that the app is working
* eventually, the object will be returned to the page, allowing it to properly render

This is a big improvement, because the integration details page is now resilient to the history and app state's state.

##### Where we are going there ~~are no roads~~ is no app state
 
What is the source of truth, the URL or the app state?  

We know that without the ID in the URL our app wouldn't work in some scenarios. But would it work without the app state?

The answer is yes, it would! When the user clicks on the "Details" link the `IntegrationDetailsPage` simply calls the right API to retrieve the data and render it.

There is only one downside with this approach, which is that the user will see a loading indicator every time the details page is visited. We didn't have this problem with the app state-based solution! Can we achieve the same result without it?

Again, the answer is yes! Under the hood, `react-router` uses the [`history API`](https://developer.mozilla.org/en-US/docs/Web/API/History_API) to modify the history and react to its changes. Whenever a `Link` is clicked, `react-router` will invoke the [`history.pushState` method](https://developer.mozilla.org/en-US/docs/Web/API/History_API#The_pushState()_method) passing the new URL.
Reading the documentation, we see that the `pushState` method can take a _state object_ as a parameter:

> The state object is a JavaScript object which is associated with the new history entry created by `pushState()`. Whenever the user navigates to the new state, a popstate event is fired, and the state property of the event contains a copy of the history entry's state object.

What's handy about this is that the serialized object will be saved by the browser and made available to the code whenever that history entry is accessed (eg. after a refresh, a browser crash).

Remember that when displaying the integrations list we already have access to the integration object. We can simply pass the the state object that goes with the URL to the `Link` object, like so:

```ts
// routes.ts
import { include } from 'named-urls';

export default {
 integrations: include('/integrations', {
   list: '',
   details: ':integrationId/details'
 })
};
```

```ts
// reducers.ts
import { reverse } from 'named-urls';
import routes from './routes';

export default {
  integrations: {
    list: () => reverse(routes.integrations.list),
    /*
      the `Link`'s `to` parameter takes either a string or an object [1]
      wich can hold a state object.
      The `details` resolver will ask for an Integration and derive the
      right params and state from that.
      [1] https://reacttraining.com/react-router/web/api/Link/to-object
    */
    details: (params: { integration: Integration }) => ({
      pathname: reverse(routes.integrations.details, {
        integrationId: params.integration.id
      }),
      state: {
        integration: params.integration
      }
    })
  }
};
```

```tsx
import resolvers from './resolvers';

<Link to={resolvers.integrations.details({ integration })}>Details</Link>
```

```tsx
// IntegrationDetailPage.tsx
import { WithLoader, WithRouteData } from '@syndesis/utils';
import { WithIntegration } from '@syndesis/api';
import { Loader } from '@syndesis/ui';

interface IIntegrationDetailPageParams {
  integrationId: string;
}

interface IIntegrationDetailPageState {
  integration?: Integration; // this could be undefined if the user comes from a shared URL
}

export class IntegrationDetailPage extends React.Component {
  public render() {
    return (
      {/* this helper will check the history object for us and return the params and state object, as by the provided interfaces */}
      <WithRouteData<IIntegrationDetailPageParams, IIntegrationDetailPageState>>
        {({ integrationId }, { integration }) =>
           {/*
             let's ask the backend to retrieve and integration with ID `integrationId`,
             and use `integration` as the initialValue.
             If `integration` is a valid object, this action will be synchronous and the
             returned `hasData` will be true, allowing us to skip any loading.
           */}
           <WithIntegration id={integrationId} initialValue={integration}>
             {({ data, hasData, error }) => (
             <WithLoader
               error={error}
               loading={!hasData}
               loaderChildren={<Loader />}
               errorChildren={<div>Ops! Something broke.</div>}
             >
               {() => (
                 {/*
                   `data` will contain our integration object, either loaded asynchronously
                   from the API or returned immediately because the initialData we
                   provided was valid
                 */}
                 <h1>{integration.name}</h1>
               )
             </WithLoader>
           </WithIntegration>
        }
      </WithRouteData>
    );
  }
}
```

We now have a page that is resilient to page refreshes, can be shared with others, and uses the URL as a single source of truth. Plus, with a little help from the browser, we can use history state to improve the user experience.

### Testing

Unit tests should ensure that components are functioning and rendering as expected by the user, not the developer. This means a lot of smoke tests for components in the UI with DOM assertions that they render and behave as expected, for their specific purpose.

E2E tests should ensure that the UI as a whole functions as expected by the user, including the process and flow. This means a lot of interaction and behavior assertions based on business requirements. We recommend writing a test for a happy path, an alternative path, and an exception path, when possible, for each use case added to the Syndesis app.

#### Unit Testing

Unit tests exist in both the [`packages/ui`](packages/ui) and [`syndesis`](syndesis) directories.

When you run `$ yarn test`, these will run automatically along with all other unit tests.

Alternatively, you can run `$ yarn test --coverage --color --runInBand` from the `./syndesis` or `./packages/ui` directories.

To run for just one package, run `$ yarn test` from within the package directory.

We use [react-testing-library](https://github.com/testing-library/react-testing-library) and [Jest](https://jestjs.io/) for unit tests. Test coverage is provided by [Istanbul](https://github.com/gotwarlost/istanbul), which comes built into Jest. Coverage reporting is enabled with the `--coverage` flag.

You can find the configuration settings for Jest in `./packages/ui/package.json` under the `jest` prop, or under `./syndesis/package.json`.


#### E2E Testing

_Configuration_
First, configure your environment. We provide a sample configuration file for your environment, located at `syndesis/cypress.env.example.json`. Duplicate (don't simply rename) this file, and name as `cypress.env.json`. By default, Cypress will load these vars for your tests. If, in turn, you'd like to override those, you always can set vars in the terminal. See [here](https://docs.cypress.io/guides/guides/environment-variables.html#Option-3-CYPRESS) for more information. In tests, you can access either type of variable like this:

```
cy.get('[data-testid=username]').type(Cypress.env('username'))
```

Where `username` would just be a property in your `cypress.env.json` file or `export CYPRESS_username=example`.

_Running Tests_
To run all E2E tests: `$ yarn e2e`

To run E2E tests directly in the terminal, no browser: `$ yarn e2e:terminal`

Cypress will check the `baseUrl` you have configured, which should be your app server. If it cannot load it, it will not validate and will not run. The `baseUrl` is defined in [`./syndesis/cypress.json`](syndesis/cypress.json), though this should already be pre-configured for you.

E2E tests are written using [Cypress](https://www.cypress.io/) and live in the [`syndesis/cypress/integration`](syndesis/cypress/integration) directory. You can find the configuration settings for Cypress under [`syndesis/cypress.json`](syndesis/cypress.json).

_E2E Test Writing Flow:_

1. Run the dev server as usual.
2. Define the happy path and use cases.
3. Write your test in `/syndesis/cypress/integration`. It must end in `.spec.js` or `.spec.ts` for Cypress to find it.
4. There are snapshots of BE responses in JSON 5 format available in [`syndesis/tapes`](syndesis/tapes), which can be used as mock data. Read more below in "Data for E2E & Integration Testing".


### Data for E2E & Integration Testing

In order to avoid extensive mocking of the app and countless server requests, we instead have a recorder for network fetch payload that will resemble E2E and integration tests as closely as possible, and can later be replayed for testing. These "tapes" are essentially snapshots of selected API responses and can be used to provide contract-compliant mock data from a specific time, which can be used for both E2E and integration tests, so long as they are used with Cypress.

The way it works is that the `syndesis` dev server interacts with a given API via a proxy set up by CRA, records the responses, and allows you to replay those responses for later use (i.e. in tests). Recordings are stored in [`syndesis/tapes/[value of the syndesis-mock-session request header]/api/v1/path/xxx.json5`](syndesis/tapes), where the value of the mock-session request header should match the name of the test file (e.g. `homepage.spec.js`).

**NOTE: Only record a session if you think you need it (e.g. writing a new test for which there are no tapes), otherwise you can create unnecessary tapes.** If a tape for your test exists but isn't working properly, record a new one.

To see this in action, you can record a session:

1. Make sure that in the browser you don't have any tabs open that make requests to the BE.
2. In the terminal, run `$ BACKEND=https://syndesis-staging.b6ff.rh-idev.openshiftapps.com yarn record`. Wait for the development server to start.
3. In a separate tab, run Cypress with `yarn e2e`.
4. In the browser window, select each test to run for which you want to record the network request payload. You should see a log of this recording in the terminal tab from step 2, and a newly generated `.json5` file in `syndesis/tapes/<name of test you have selected>`.

To replay a session (run Cypress tests using the tapes, disallowing outbound requests to the API server):

`$ BACKEND=https://syndesis-staging.b6ff.rh-idev.openshiftapps.com yarn replay` or `$ yarn replay <session-name>`

NOTE: These tapes are just like typical test snapshots, and should be treated as code. They are ultimately committed with the rest of your code.

For more information on the BE recorder/replayer, please see [this](https://github.com/syndesisio/syndesis-react/pull/106) PR.

#### Adding Test Identifiers

UI components that have user interaction should define a `data-testid` attribute. Here are a few components that might need a test identifier: `a`, `button`, `Button`, `ButtonLink`, `Card`, `DropdownItem`, `FormControl`, `Grid.Row`, `input`, `Label`, `li`, `Link`, `ListViewItem`, `PfNavLink`, `PfVerticalNavItem`, and `Title`.

Test identifiers that do not contain any user-defined text should be a hardcoded string. **Only alphanumeric characters and hyphens should be used.** A common convention, though not mandatory, is to format the identifier using the file component name and the widget whose ID is being set. For example, a `data-testid` value of `api-connector-details-form-cancel-button` might be found on a button in the `ApiConnectorDetailsForm` component.

When a test identifier needs to contain some user-defined text, like for a card, list item, or row, the `toValidHtmlId` utility function, found in the `@syndesis/ui/helpers.ts` file, should be used to format the user-defined text. This function ensures the identifier only contains valid characters. Here is an example:

```tsx
data-testid={`integrations-list-item-${toValidHtmlId(
  this.props.integrationName
)}-list-item`}
```

The above code produces this test ID an integration with the name of "My Integration": `integrations-list-item-my-integration-list-item`.

## License

[Apache](LICENSE.txt)
