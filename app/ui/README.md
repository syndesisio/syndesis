# Syndesis UI

[![CircleCI](https://circleci.com/gh/syndesisio/syndesis-ui.svg?style=svg)](https://circleci.com/gh/syndesisio/syndesis-ui) [![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)

The front end application or UI for Syndesis - a flexible, customizable, cloud-hosted platform that provides core integration capabilities as a service. It leverages open source technologies like Apache Camel and OpenShift to provide a rock-solid user experience.

For the middle tier API that this client communicates with, please see the [syndesis-rest](https://github.com/syndesisio/syndesis/tree/master/rest) dir.

## Table of Contents

* [Quick Start](#quick-start)
* [Day-to-Day Workflow](#day-to-day-workflow)
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


## Quick Start
You can follow these steps if it's your first time setting up Syndesis, or if you want a fresh local installation to replace an existing one.

1. Make sure you have installed [node](https://nodejs.org/en/download/) version >= 6.x.x and [Yarn](https://yarnpkg.com/en/docs/install) version >= 0.18.1.

2. Get a developer deployment of Syndesis running in a Minishift environment as described in the
[Syndesis Quickstart](https://syndesis.io/quickstart/). Most are specific to your environment, so follow the links below for a quick setup:

- [Install a hypervisor for Minishift](https://docs.openshift.org/latest/minishift/getting-started/installing.html#install-prerequisites). For macOS, we recommend using the Docker xhyve plugin [here](https://docs.openshift.org/latest/minishift/getting-started/setting-up-driver-plugin.html#xhyve-driver-install), which can be installed using Homebrew.
- [Install Minishift](https://docs.openshift.org/latest/minishift/getting-started/installing.html#installing-instructions). For macOS, we recommend you use the Homebrew method.

3. Get a local developer UI with hot reloading, using the Syndesis backend running on Minishift:

```bash
# Clone our repo:
$ git clone https://github.com/syndesisio/syndesis-ui.git

# Change directory to Syndesis:
$ cd syndesis-ui

# Start up Minishift
$ minishift start

# Make sure Minishift is running:
$ minishift status

# Which should look like:
Minishift:  Running
Profile:    minishift
OpenShift:  Running (openshift v3.6.0+c4dd4cf)
DiskUsage:  11% of 17.9G

# Log into OpenShift with developer account
# Password: whatever you like
$ oc login -u developer

# Set environment variables to point to Minishift resources:
$ eval $(minishift oc-env)
$ eval $(minishift docker-env)

# For a fresh install of Syndesis:
$ ./scripts/syndesis-install --clean --pull

# Set OpenShift routes
$ ./scripts/minishift-setup.sh

# Install the dependencies:
$ yarn install

# Start the UI server:
$ yarn start:minishift
```

The `yarn start:minishift` command works when it can properly detect your local development machine's IP address.  A proxy server inside the minishift deployment will use that IP address to connect back to the development server being run by the yarn command.  If detection of the IP is failing for you, then set the `SYNDESIS_DEV_LOCAL_IP` env variable to your local machine's IP address before running the yarn `yarn start:minishift` command.

Open the Syndesis UI in your browser from the command line by running:

```bash
# To connect with Syndesis backend you don't have to use http://localhost:4200 url.
# The url has a structure similar to http://syndesis-ui-default.192.168.42.205.nip.io

# You can try to obtain it directly from Minishift configuration with one of the following commands.

# on macOS
$ open https://$(oc get routes syndesis --template "{{.spec.host}}")

# on linux
$ xdg-open https://$(oc get routes syndesis --template "{{.spec.host}}")

# on windows
$ start https://$(oc get routes syndesis --template "{{.spec.host}}")
```

A smoke test to verify you are ready to work is to add any content at the beginning of `src/app/app.component.html` and verify you see the modification in the main page of the application.

If you are having issues with Minishift, you can also use `https://0.0.0.0:4200/` to access the UI for quick development, but it will not use Minishift resources, so the app will not work properly.

In a separate tab, you might want to run unit tests and lint checks as you code. See below for more information on how to do that.

### Day-to-Day Workflow

```bash
# Start up Minishift
$ minishift start

# Log into OpenShift with developer account
# Password: whatever you like
$ oc login -u developer

# Set environment variables to point to Minishift resources:
$ eval $(minishift oc-env)
$ eval $(minishift docker-env)

# Start the UI server:
$ yarn start:minishift
```

Follow the instructions above for opening the Syndesis UI in your browser.

At the end of the day you might want to stop Minishift:
`$ minishift stop`


### Technology Stack

Included in this stack are the following technologies:

* Language: [TypeScript](http://www.typescriptlang.org) (JavaScript with @Types)
* Framework: [Angular](https://angular.io/)
* Module Bundler: [Angular CLI](https://cli.angular.io)
* Design Patterns: [PatternFly Angular](https://github.com/patternfly/patternfly-ng) (Design Consistency)
* Testing: [Cucumber.js](https://cucumber.io/) (BDD Unit Test Framework), [Karma](https://karma-runner.github.io/1.0/index.html) (Unit Test Runner), [Istanbul](https://github.com/gotwarlost/istanbul) (Code Coverage)
* Linting: [TsLint](https://github.com/palantir/tslint) (Linting for TypeScript)
* Logging: [typescript-logging](https://github.com/mreuvers/typescript-logging) (TypeScript Logging)
* Code Analysis: [Codelyzer](https://github.com/mgechev/codelyzer) (TsLint rules for static code analysis of Angular TypeScript projects)

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

### Formatting

To make sure your code is formatted consistently with the rest of the team's, you can run the following command to prettify it:

`yarn format`

### Committing Changes

The versioning workflow is under redesign at this moment, same as the different automated checks involved. Please keep an eye on this section for more up-to-date information to be released soon.

### Documentation

We use [Compodoc](https://compodoc.github.io/website/) for documentation, or [here](https://compodoc.github.io/website/guides/comments.html) to see how to format comments. Files get generated automatically in the `/documentation` directory. Read the documentation [here](https://compodoc.github.io/website/guides/getting-started.html) to see how to properly document features. You can automatically generate and run docs using Yarn:

`yarn compodoc`

Or manually with `compodoc -s`, or `compodoc` if you want it to simply generate the files in the default `/documentation` directory and run it with an HTTP server.

#### Production
*Requires having `angular-cli` installed globally.*

`ng serve --prod -aot`

## Testing

### Watch and Run Unit Tests

`ng test` or `yarn test`

### Run Tests without Watch

`ng test --watch=false`


### Linting

To run the linter: `yarn lint`

## Configuring

Configuration files live in `/config`. Configuration files are currently available for Webpack, Karma, and Protractor.

## Contributing

Pull requests are always welcome. Please read through our [Contribution](https://syndesis.io/community/contributing/) guidelines for submitting improvements.

New feature development should generally follow [these guidelines](https://syndesis.io/community/engineering_guidelines/).


## Data Mapper

To update the data mapper, change the commit SHA in package.json to pick up the desired code revision, then run `yarn install`.  Verify that the projects still builds by running `yarn start:prod` and adjust the code as necessary based on the data mapper example usage in the [README](https://github.com/atlasmap/atlasmap-ui).

When updating the SHA you should also run `yarn copy:assets` to bring over any assets the data mapper component needs, this tasks copies them all to `src/assets/dm`
