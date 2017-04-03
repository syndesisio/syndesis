# Cucumber e2e tests

To run tests first start local web server
```bash
yarn
yarn start
```

create `e2e/data/users.json` file with your github login credentials

launch tests using local dev server `http://localhost:4200`
```bash
yarn e2e
```

launch tests using remote web ui
```bash
export IPAAS_UI_URL='https://ipaas-qe.b6ff.rh-idev.openshiftapps.com/'
yarn e2e:ipaas-qe
```

alternatively launch tests in Docker container with Xvfb
```bash
export IPAAS_UI_URL='https://ipaas-qe.b6ff.rh-idev.openshiftapps.com/'
yarn e2e:xvfb
```


## Launch subset of cucumber tests
Tests (`*.feature) files can have something like java annotations.
In the cucumber docs it's called [tags](https://github.com/cucumber/cucumber/wiki/Tags).


Example of feature with tag `@narrative`
```gherkin
@narrative
Feature: First pass at login, homepage, connections, and integrations
  https://issues.jboss.org/browse/IPAAS-153
```https://yarnpkg.com/lang/en/docs/cli/run/

Can be run with command
```bash
# first -- tells yarn to pass these arguments to script
yarn e2e -- --cucumberOpts.tags="@narrative"
```

For more information about parameters see [yarn run docs](https://yarnpkg.com/lang/en/docs/cli/run/).
