# Syndesis

Syndesis is a single page application built with React.

## Table of Contents

* [Architecture](#architecture)
  * [syndesis](#syndesis-1)
    * [syndesis-context](#syndesis-context)
  * [packages](#packages)
    * [api](#packagesapi)
    * [models](#packagesmodels)
    * [ui](#packagesui)
    * [utils](#packagesutils)
    * [dashboard](#packagesdashboard)
    * [connections](#packagesconnections)
    * [integrations](#packagesintegrations)
  * [typings](#typings)
* [First time setup](#first-time-setup)
* [Building the project](#building-the-project)
* [Development mode](#development-mode)

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

It also provides an [API for sub-apps](#syndesis-context) - in the form of a [React's Context](https://reactjs.org/docs/context.html) - for 
interact with it, eg. closing the navigation bar, or redirecting to another sub-app.

It's built with [create-react-app](https://github.com/facebook/create-react-app).

### syndesis-context

TBD. 

###  packages/*

#### packages/api

This package contains a collection of React Components implementing the [render props pattern](https://reactjs.org/docs/render-props.html)
to ease interacting with Syndesis's Backend. 

#### packages/models

This package contains the Typescript definitions of the models as read from the backend.

#### packages/ui

This package contains a collection UI elements that are common across the application. 

All the elements are written as React PureComponents or Stateless Functional Components. The idea is to decouple the 
presentation from the model that holds the data that needs to be presented to promote code reuse and easing the testing
efforts. 

#### packages/utils

This package contains commonly used components of function that don't fit any of the above packages.

#### packages/dashboard

This is the sub-app that implements the Dashboard section.

#### packages/connections

This is the sub-app that implements the Connections section.

#### packages/integrations

This is the sub-app that implements the Integrations section.

### typings

Extra typings for pure Javascript dependencies that should eventually be pushed on [DefinitelyTyped](https://github.com/DefinitelyTyped/DefinitelyTyped/).

## First time setup 

[Yarn](https://yarnpkg.com) is the package manager required to work on the project.

To install all the dependencies: 

```
yarn install
```

## Building the project

To build syndesis and all the packages:

```
yarn build
```

## Development mode

To start the development server for `syndesis` and watch for changes in any of the packages:

```
yarn watch
```

To start the development server only for `syndesis`:

```
yarn watch:app
```

To start the development server only for the packages:

```
yarn watch:packages
```

To start the development server for a specific package:

```
yarn watch:packages --scope @syndesis/package-name
```
_Where `@syndesis/package-name` is the name in the `package.json`_



## Roadmap

- [ ] Extend the build system for the packages to extract any CSS file referenced in the project and make it available 
in the dist folder.
- [ ] Implement [syndesis-context](#syndesis-context)
- [ ] ...so many things!

## License

[Apache](LICENSE.txt)