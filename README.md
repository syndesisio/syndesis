# Syndesis UI

[![CircleCI](https://circleci.com/gh/syndesisio/syndesis-ui.svg?style=svg)](https://circleci.com/gh/syndesisio/syndesis-ui) [![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)

The front end application or UI for Syndesis - a flexible, customizable, cloud-hosted platform that provides core integration capabilities as a service. It leverages open source technologies like Apache Camel, OpenShift and Kubernetes to provide a rock-solid user experience.

For the middle tier API that this client communicates with, please see the [syndesis-rest](https://github.com/syndesisio/syndesis-rest) repo.

## UI Developer Quick Start

**Make sure you have installed [node](https://nodejs.org/en/download/) version >= 6.x.x and [Yarn](https://yarnpkg.com/en/docs/install) version >= 0.18.1**

First get a developer deployment of Syndesis running in a minishift environment as described in the
[Syndesis Quickstart](https://syndesis.io/quickstart/).  Then do the following to get a local developer UI with hot reloading running against the Syndesis backend running in minishift:

```bash
# clone our repo
git clone https://github.com/syndesisio/syndesis-ui.git

# change directory to Syndesis
cd syndesis-ui

# Configure the UI and Minishfit for dev mode.
./scripts/minishift-setup.sh

# install the dependencies
yarn

# start the server
yarn start:minishift
```

Bring up the syndesis console in your browser.  You can start load it from the command line by running:

```bash
open http://$(oc get routes syndesis --template "{{.spec.host}}")
```

## Table of Contents

* [Technology Stack](#technology-stack)
* [File Structure](#file-structure)
* [Getting Started](#getting-started)
  * [Dependencies](#dependencies)
  * [Installing](#installing)
  * [Running](#running)
  * [Testing](#testing)
  * [Configuring](#configuring)
* [Contributing](#contributing)
* [Resources](#resources)
* [Data Mapper Updates](#data-mapper)

### Technology Stack

Included in this stack are the following technologies:

* Language: [TypeScript](http://www.typescriptlang.org) (JavaScript with @Types)
* Framework: [Angular 2](https://angular.io/)
* Module Bundler: [Angular CLI](https://cli.angular.io)
* Design Patterns: [PatternFly](https://www.patternfly.org/)
* Testing: [Cucumber.js](https://cucumber.io/) (BDD Unit Test Framework), [Karma](https://karma-runner.github.io/1.0/index.html) (Unit Test Runner), [Istanbul](https://github.com/gotwarlost/istanbul) (Code Coverage)
* Linting: [TsLint](https://github.com/palantir/tslint) (Linting for TypeScript)
* Logging: [typescript-logging](https://github.com/mreuvers/typescript-logging) (TypeScript Logging)
* Code Analysis: [Codelyzer](https://github.com/mgechev/codelyzer) (TsLint rules for static code analysis of Angular 2 TypeScript projects)
* Charts: [ng2-charts](https://github.com/valor-software/ng2-charts) (Data Visualization)

### File Structure

We use the component approach in our starter. This is the new standard for developing Angular apps and a great way to ensure maintainable code by encapsulation of our behavior logic. A component is basically a self contained app, usually in a single file or a folder with each concern as a file: style, template, specs, and component class.

```plain
syndesis-ui/
 │
 ├──docs/                         * our documentation
 |   ├──commands.md               * additional cli commands available to us
 |   ├──contributing.md           * contribution guidelines
 |   ├──entities.md               * entities/models and their relationships for reference
 │   ├──faq.md                    * frequently asked questions about using syndesis
 │   ├──overview.md               * a technical overview for understanding the project
 │   └──typescript.md             * some typescript tips and resources
 │
 ├──src/                          * our source files that will be compiled to javascript
 │   │
 │   ├──app/                      * our Angular 2 application
 │   │   │
 │   │   ├──user/                 * an example 'user' component, based on an entity/model. can be nested further.
 │   │   │   ├──user.component.ts * the primary Angular component file; essentially a controller + directive combined
 │   │   │   ├──user.html         * our HTML user template for the user component
 │   │   │   └──user.spec.ts      * our unit test for the user component
 │   │   │
 │   │   ├──app.component.ts      * a simple version of our App component components
 │   │   └──app.spec.ts           * a simple test of components in app.ts
 │   │
 │   ├──assets/                   * static assets are served here
 │   │   ├──robots.txt            * for search engines to crawl your website
 │   │   └──service-worker.js     * ignore this. Web App service worker that's not complete yet
 │   │
 │   ├──polyfills.ts      * our polyfills file
 │   └--index.html                * our primary layout that contains subviews
 │
 ├──.gitignore                    * let git know which files to ignore and not stage for commit
 ├──karma.conf.js                 * karma, our test runner, config file
 ├──LICENSE                       * Syndesis is available for use under the Apache 2.0 license
 ├──npm-shrinkwrap.json           * npm's way of allowing us to control exact versions of dependencies
 ├──package.json                  * what npm uses to manage it's dependencies
 ├──README.md                     * this exact file :)
 ├──tsconfig.json                 * typescript compiler config
 ├──tslint.json                   * typescript lint config
```

## Getting Started

### Dependencies

What you need to run this app:

* `node` (`brew install node` for OS X users)
* `yarn` (see [https://yarnpkg.com/en/docs/install](https://yarnpkg.com/en/docs/install))
* `angular-cli` (optional, but useful for development. see [here](https://cli.angular.io/))
* Ensure you're running the latest versions Node `v6.x.x`+ and Yarn

You do *not* need to install Angular CLI globally, but we recommend it if you'd like to use the [convenient commands](https://cli.angular.io/reference.pdf) it provides, or any of the `ng` commands we reference below.

### Installing

* `fork` the syndesis repo
* `clone` your fork
* `yarn` to install all dependencies
* `yarn start` or `ng serve` to start the dev server

### Running

After you have installed all dependencies you can now run the app. Run `yarn start` or `ng serve` to start a local server which will watch, build (in-memory), and reload for you. The port will be displayed to you as `http://0.0.0.0:4200` (or if you prefer IPv6, then it's `http://[::1]:4200/`).

#### Development

`yarn start`

Or, with angular-cli:

`ng serve`

### Committing changes

The repo is commitizen friendly, after making some changes:

`yarn commit`

to commit them. `yarn commit` is the same as running `git cz` in this case. So, you can use all the git commit options, 
for example: `yarn commit -am 'Blah blah blah''`.

Don't forget to add your files to staging first with `git add -A`. This is a `git commit` tool, not a total `git` replacement.

### Documentation

We use [Compodoc](https://compodoc.github.io/website/) for documentation, or [here](https://compodoc.github.io/website/guides/comments.html) to see how to format comments. Files get generated automatically in the `/documentation` directory. Read the documentation [here](https://compodoc.github.io/website/guides/getting-started.html) to see how to properly document features. You can automatically generate and run docs using Yarn:

`yarn compodoc`

Or manually with `compodoc -s`, or `compodoc` if you want it to simply generate the files in the default `/documentation` directory and run it with an HTTP server.

#### Production
*Requires having `angular-cli` installed globally.*

`ng serve --prod -aot`

For a list of common commands, see [here](/docs/commands.md).

## Testing

### Watch and Run Unit Tests

`ng test` or `yarn test`

### Run Tests without Watch

`ng test --watch=false`

For a list of common commands, see [here](/docs/commands.md).

### Linting

To run the linter: `yarn lint`

## Configuring

Configuration files live in `/config`. Configuration files are currently available for Webpack, Karma, and Protractor.

## Contributing

Pull requests are always welcome. Please read through our [Contribution](https://syndesis.io/community/contributing/) guidelines for submitting improvements.

New feature development should generally follow [these guidelines](https://syndesis.io/community/engineering_guidelines/).

## Frequently Asked Questions (FAQ)

You can read our FAQ, located in our `/docs` directory, [here](/docs/faq.md).

## Data Mapper

To update the data mapper, change the commit SHA in package.json to pick up the desired code revision, then run `yarn install`.  Verify that the projects still builds by running `yarn start:prod` and adjust the code as necessary based on the data mapper example usage in the [README](https://github.com/atlasmap/atlasmap-ui).

When updating the SHA you should also run `yarn copy:assets` to bring over any assets the data mapper component needs, this tasks copies them all to `src/assets/dm`
