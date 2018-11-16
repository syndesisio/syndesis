# Syndesis

Syndesis is a single page application built with React.

## Table of Contents

* [Architecture](#architecture)
  * [syndesis](#syndesis-1)
  * [packages](#packages)
      * [api](#packagesapi)
      * [models](#packagesmodels)
      * [ui](#packagesui)
      * [utils](#packagesutils)
      * [syndesis-context](#packagessyndesis-context)
  * [typings](#typings)
* [Installation](#installation)
* [Building](#building)
* [Scripts](#scripts)
* [Roadmap](#roadmap)

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

## Roadmap

- [ ] Extend the build system for the packages to extract any CSS file referenced in the project and make it available 
in the dist folder.
- [ ] Implement [syndesis-context](#packagessyndesis-context)
- [ ] ...so many things!

## License

[Apache](LICENSE.txt)