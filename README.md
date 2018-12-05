# Syndesis

Syndesis is a single page application built with React.

## Table of Contents

- [Syndesis](#syndesis)
  - [Table of Contents](#table-of-contents)
  - [Architecture](#architecture)
    - [syndesis](#syndesis)
      - [Development server](#development-server)
        - [Resetting the configuration](#resetting-the-configuration)
    - [packages](#packages)
      - [packages/api](#packagesapi)
        - [Development server](#development-server-1)
      - [packages/models](#packagesmodels)
      - [packages/ui](#packagesui)
        - [Development server](#development-server-2)
        - [Storybook](#storybook)
      - [packages/utils](#packagesutils)
        - [Development server](#development-server-3)
    - [packages/syndesis-context](#packagessyndesis-context)
    - [typings](#typings)
  - [Installation](#installation)
        - [Development server](#development-server-4)
  - [Building](#building)
  - [Scripts](#scripts)
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
  - [Roadmap](#roadmap)
  - [License](#license)

## Architecture 

We use [Lerna](https://github.com/lerna/lerna) to streamline the development process; the most common operations, 
like building the project or running the development mode can be done directly from the project root.  

Code is split in many packages, organized as a monorepo using [Yarn's workspaces](https://yarnpkg.com/lang/en/docs/workspaces/). 
The workspace is configured like this:

```
"syndesis",
"packages/*"
"typings/*"
```

### syndesis

`syndesis` is the main application. It handles the authentication against Syndesis's OAuth Server, and provides the main
 app layout where "sub-apps" can be injected. 

![](doc/assets/syndesis-chrome.png)

It also provides an [API for sub-apps](#packagessyndesis-context) - in the form of a [React's Context](https://reactjs.org/docs/context.html) - for 
interact with it, eg. closing the navigation bar, or redirecting to another sub-app.

It's built with [create-react-app](https://github.com/facebook/create-react-app).

#### Development server

```bash
$ yarn watch:app
```

##### Resetting the configuration

Just clear the local storage for the localhost:3000 origin. You can do it with Chrome Dev Tools opened on the running app,
or you can do it from Chrome's special url chrome://settings/siteData  

###  packages

#### packages/api

This package contains a collection of React Components implementing the [render props pattern](https://reactjs.org/docs/render-props.html)
to ease interacting with Syndesis's Backend.

##### Development server

```bash
$ yarn watch:packages --scope @syndesis/api
```
 

#### packages/models

This package contains the Typescript definitions of the models as read from the backend.


#### packages/ui

This package contains a collection UI elements that are common across the application. 

All the elements are written as React PureComponents or Stateless Functional Components. The idea is to decouple the 
presentation from the model that holds the data that needs to be presented to promote code reuse and easing the testing
efforts. 

##### Development server

```bash
$ yarn watch:packages --scope @syndesis/ui
```

##### Storybook

This package provides a [Storybook](https://storybook.js.org) to develop and document the components in isolation.  
Storybook can be launched like this:

```bash
# From the package folder
$ yarn storybook
```
  
A browser tab should eventually be opened pointing on [http://localhost:90001](http://localhost:90001).

#### packages/utils

This package contains commonly used components of function that don't fit any of the above packages.

##### Development server

```bash
$ yarn watch:packages --scope @syndesis/utils
```


### packages/syndesis-context

TBD. 

### typings

Extra typings for pure Javascript dependencies that should eventually be pushed on [DefinitelyTyped](https://github.com/DefinitelyTyped/DefinitelyTyped/).

## Installation 

[Yarn](https://yarnpkg.com) is the package manager required to work on the project.

To install all the dependencies: 

##### Development server

```
yarn install
```

## Building

To build syndesis and all the packages:

```bash
yarn build
```

## Scripts

To start the development server for `syndesis` and watch for changes in any of the packages:

```bash
$ yarn watch
```
_**IMPORTANT:** you must have successfully built all the packages _before_ running the watch command to successfully 
run this command._    
_**IMPORTANT:** this will change the `syndesis-ui` POD to point to your development server. To 
restore the POD to the original state, you will have to manually run `yarn minishift:restore`_

To start the development server only for `syndesis`:

```bash
$ yarn watch:app
```
_The development server for the app will not be available at [http://localhost:3000](http://localhost:3000)_  
_**IMPORTANT:** you must have successfully built all the packages _before_ running the watch command to successfully 
run this command._    
_**IMPORTANT:** this will change the `syndesis-ui` POD to point to your development server. To 
restore the POD to the original state, you will have to manually run `yarn minishift:restore`_

To start the development server only for the packages:

```bash
$ yarn watch:packages
```

To start the development server for a specific package you can pass the package name to the previous command:

```bash
$ yarn watch:packages --scope @syndesis/package-name
```

To run the test suite:

```bash
$ yarn test
```

To run the test suite for a specific package you can pass the package name to the previous command:

```bash
$ yarn test --scope @syndesis/package-name
```

## Internationalization

We are using the [react-i18next library](https://github.com/i18next/react-i18next) for internationalization (i18n). You can find documentation about this library at [react.i18next.com](https://react.i18next.com).

The [syndesis package](#architecture) is the **only** package where we are using this *i18n* library. To make this *i18n* library available in the `syndesis` package, the `syndesis/package.json` file was edited as follows:
- added dependency to [i18next](https://github.com/i18next/i18next) - the core library that handles all the translation functionality.
- added a dependency to [i18next-browser-languagedetector](https://github.com/i18next/i18next-browser-languageDetector) - detects the browser's locale and sets it as the default locale for translations.
- added dependency to [react-i18next](https://github.com/i18next/react-i18next) - built on top of the core library and provides functionality specific to *React*.
- added `@types/i18next` to `devDependencies`.
- added `@types/i18next-browser-languagedetector` to `devDependencies`.

If [packages](#architecture) other than the `syndesis` package require translations, the already translated text should be passed into those packages using the custom properties of the component. **Do not modify those packages' `package.json` to add dependencies to the *i18n* framework!** We are doing it this way to facilitate testing and to remove the impact of changing the *i18n* library if ever we need to do that. Here is an example of how you would do that:

```react
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

In order to get internalization working in our app, a specific [i18next instance](https://react.i18next.com/components/i18next-instance) needs to be configured. We do this in the `syndesis/src/i18n` folder. Here is how that folder is organized:
- `index.ts` - configures our *i18next instance*. You can find more information about `i18next` settings at [i18next.com](https://www.i18next.com/overview/configuration-options) and the `react-i18n` additional settings at [react.i18next.com](https://react.i18next.com/components/i18next-instance).
- `locales/` - a folder that sets up the *i18n* namespaces and contains the translations shared within the `syndesis/src` codebase.
- `locales/index.ts` - contains references to all the app translations files.
- `locales/shared-translations.en.json` - contains the shared English translations and placeholders for the namespaces provided from within `syndesis/src`.
- `locales/shared-translations.it.json` - contains the shared Italian translations and placeholders for the namespaces provided from within `syndesis/src`.

Now that we have our *i18n instance* configured, we can start *i18n*-ing our code. This is done by adding a `locales/` folder at the root of the code that you want to have its own, non-shared translations. This folder defines your translations and your namespace. Now, all that is left to do is to add specific calls to the *i18n* framework from you code.

**Important**: Adding new namespaces does require changes to be made to the *i18n instance* described above.

**Note**: All namespaces are actually always available by qualfiying the translation key with the namespace. 

When a method returns a *React* element or fragment (like `render()` does), and translations are needed, the [NamespacesConsumer](https://react.i18next.com/components/namespacesconsumer) class is used. Using the `NamespacesConsumer` class in the method gives that method access to the *i18n* framework. It does this by exposing the `t`, or translation function. An array of namespaces, which are setup by your *i18n instance*, is used by the `NamespacesConsumer` to perform the translations. The first namespace in the array **does not** require its keys to be qualified. However, any subsequent namespaces **do** require their keys to be qualified. An example of internationalizing a method like this can be found [here](#internationalizing-a-render-method).

When translations are needed but you are not in a method that returns a *React* element, or you are in code that is not a method at all, the `NamespacesConsumer` cannot be used. Instead, the *i18n instance* itself is used. For instance you can translate some text in a `const` that is constructing an instance of an `interface`. See [this](#internationalizing-text-in-a-constant) example.

The following sections give you examples on how to *i18n* things.

#### Internationalizing a render method

To internationalize a `render()` method, do the following:

1. Add this import statement 
```react
import { NamespacesConsumer } from 'react-i18next';
```
1. Wrap what normally would be returned with a `NamespacesConsumer` tag. You need to set the array of namespaces you will be using. The first namespace is the default and does not need to be used when referencing a translation key. Translation keys not found in the default namespace need to be qualified. See [this](#translations-using-different-namespaces) for examples of using namespaces in translations.
```react
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
3. Add translations into your translation files.
4. Use the `t` function in the `render()` method wherever a translation is needed. [Here](#translation-examples) you will find examples of how to use the `t` function.

#### Internationalizing text in a constant

To internationalize text in a `const`, do the following:

1. Add this import statement:
```react
import i18n from 'relative-path-to-syndesis/src/i18n'; 
```
2. Add translations into your translation files.
3. Use the `i18n.t` function to perform the translations. [Here](#translation-examples) you will find examples of how to use the `t` function.
```react
const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;
```

#### Translation Examples

Some examples taken from [i18next.com](https://www.i18next.com/translation-function/nesting) documentation.

##### Simple translation using default namespace
- translation file
```json
{
  "errorMsg": "An error occurred.",
}
```
- usage
```react
{ t('erroMsg') }
```
- outputs: "An error occurred."

##### Translations using different namespaces
- usage
```react
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
```react
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
```react
{ t('nesting1') }
```
- outputs: "1 2 3"

##### Translation as an argument to another translation
- translation file
```json
{
      "key1": "hello world",
      "key2": "say: {{val}}"
}```
- usage
```react
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

## Roadmap

- [ ] Extend the build system for the packages to extract any CSS file referenced in the project and make it available 
in the dist folder.
- [ ] Implement [syndesis-context](#packagessyndesis-context)
- [ ] ...so many things!

## License

[Apache](LICENSE.txt)