# Red Hat iPaaS Client

[![CircleCI](https://circleci.com/gh/redhat-ipaas/ipaas-client.svg?style=svg)](https://circleci.com/gh/redhat-ipaas/ipaas-client)

The front end application for Red Hat iPaaS - a flexible, customizable, cloud-hosted platform that provides core integration capabilities as a service. It leverages Red Hat's existing product architecture using OpenShift Online/Dedicated and Fuse Integration Services.

For the middle tier API that this client communicates with, please see [this](https://github.com/redhat-ipaas/ipaas-api-java) repo.

Included in this stack are the following technologies:

* Language: [TypeScript](http://www.typescriptlang.org) (JavaScript with @Types)
* Framework: [Angular 2](https://angular.io/)
* Module Bundler: [Angular CLI](https://cli.angular.io)
* Design Patterns: [PatternFly](https://www.patternfly.org/)
* Data Visualization: [C3](http://c3js.org/)
* Testing: [Jasmine](http://jasmine.github.io/) (BDD Unit Test Framework), [Karma](https://karma-runner.github.io/1.0/index.html) (Unit Test Runner), [Protractor](http://www.protractortest.org/#/) (E2E Framework), [Istanbul](https://github.com/gotwarlost/istanbul) (Code Coverage)
* Linting: [TsLint](https://github.com/palantir/tslint) (Linting for TypeScript)
* Logging: [js-Logger](https://github.com/jonnyreeves/js-logger) (JavaScript Logger)
* Code Analysis: [Codelyzer](https://github.com/mgechev/codelyzer) (TsLint rules for static code analysis of Angular 2 TypeScript projects)

## Quick Start

**Make sure you have node version >= 6.x.x and Yarn version >= 0.18.1**

Clone/download the repo start editing `app.component.ts` inside [`/src/app/`](/src/app/app.component.ts)

```bash
# clone our repo
git clone https://github.com/redhat-ipaas/ipaas-client.git

# change directory to iPaaS
cd ipaas-client

# install the dependencies
yarn

# start the server
npm start
```

Go to [http://0.0.0.0:4200](http://0.0.0.0:4200) or [http://localhost:4200](http://localhost:4200) in your browser.

## Table of Contents

* [File Structure](#file-structure)
* [Getting Started](#getting-started)
  * [Dependencies](#dependencies)
  * [Installing](#installing)
  * [Running](#running)
  * [Testing](#testing)
  * [Configuring](#configuring)
* [Contributing](#contributing)
* [Resources](#resources)


### File Structure

We use the component approach in our starter. This is the new standard for developing Angular apps and a great way to ensure maintainable code by encapsulation of our behavior logic. A component is basically a self contained app, usually in a single file or a folder with each concern as a file: style, template, specs, e2e, and component class.

```plain
ipaas-client/
 │
 ├──docs/                         * our documentation
 |   ├──commands.md               * additional cli commands available to us
 |   ├──contributing.md           * contribution guidelines
 |   ├──entities.md               * entities/models and their relationships for reference
 │   ├──faq.md                    * frequently asked questions about using ipaas
 │   ├──overview.md               * a technical overview for understanding the project
 │   └──typescript.md             * some typescript tips and resources
 │
 ├──src/                          * our source files that will be compiled to javascript
 │   │
 │   ├──app/                      * our Angular 2 application
 │   │   │
 │   │   ├──user/                 * an example 'user' component, based on an entity/model. can be nested further.
 │   │   │   ├──user.component.ts * the primary Angular component file; essentially a controller + directive combined
 │   │   │   ├──user.e2e.ts       * our e2e test for the user component
 │   │   │   ├──user.html         * our HTML user template for the user component
 │   │   │   └──user.spec.ts      * our unit test for the user component
 │   │   │
 │   │   ├──app.component.ts      * a simple version of our App component components
 │   │   ├──app.e2e.ts            * a simple end-to-end test for /
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
 ├──LICENSE                       * iPaaS is available for use under the Apache 2.0 license
 ├──npm-shrinkwrap.json           * npm's way of allowing us to control exact versions of dependencies
 ├──package.json                  * what npm uses to manage it's dependencies
 ├──protractor.conf.js            * protractor, our e2e testing framework, config file
 ├──README.md                     * this exact file :)
 ├──tsconfig.json                 * typescript compiler config
 ├──tslint.json                   * typescript lint config
```

## Getting Started

### Dependencies

What you need to run this app:

* `node` (`brew install node` for OS X users)
* `yarn` (see [https://yarnpkg.com/en/docs/install](https://yarnpkg.com/en/docs/install))
* Ensure you're running the latest versions Node `v6.x.x`+ and Yarn

### Installing

* `fork` the ipaas repo
* `clone` your fork
* `yarn` to install all dependencies
* `ng serve` to start the dev server

### Running

After you have installed all dependencies you can now run the app. Run `npm start` to start a local server which will watch, build (in-memory), and reload for you. The port will be displayed to you as `http://0.0.0.0:4200` (or if you prefer IPv6, then it's `http://[::1]:4200/`).

#### Development

```
npm start
```

#### Production
Requires having `angular-cli` installed globally.

```
ng serve --prod -aot
```

For a list of common commands, see [here](/docs/commands.md).

## Testing

### Watch and Run Tests

```
npm test
```

### Run Tests
Requires having `angular-cli` installed globally.

```
ng test --watch=false
```

For a list of common commands, see [here](/docs/commands.md).

## Configuring

Configuration files live in `/config`. Configuration files are currently available for Webpack, Karma, and Protractor.

## Contributing

Pull requests are always welcome. Please read through our [Contribution](/docs/contributing.md) guidelines in the `/docs` directory.

## Resources

Resources used in planning and developing this project.

* [Backend of iPaaS](https://github.com/fabric8io/fabric8-forge)
* [Design Prototype](https://projects.invisionapp.com/share/4P84NS9K6#/screens)
* [Entity Relationships](/docs/entities.md)
* [Google Drive iPaaS Folder](https://drive.google.com/drive/folders/0B8Kpb4FsPn_fQ3NsOVRlNzIzZTg?usp=sharing)
* [User Flow](https://drive.google.com/a/redhat.com/file/d/0B5uwxxDGbUVzNTl4aFQ4NVNnWlE/view)

## Frequently Asked Questions (FAQ)

You can read our FAQ, located in our `/docs` directory, [here](/docs/faq.md).
