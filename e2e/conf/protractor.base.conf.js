// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

let reporter = require('cucumber-html-reporter');
let fse = require('fs-extra');
let path = require('path');
const os = require('os');

// define paths for cucumber test framework
let testPath = path.resolve('e2e/');
let reportsPath = path.join(testPath, 'cucumber-reports/');
let cucumberJsonPath = path.join(reportsPath, 'cucumber-report.json');
let cucumberHtmlPath = path.join(reportsPath, 'cucumber-report.html');


exports.config = {
  allScriptsTimeout: 11000,
  specs: [
    testPath + '/**/*.feature'
  ],
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'args': ['--no-sandbox']
    }
  },
  directConnect: true,
  baseUrl: 'http://localhost:4200/',
  framework: 'custom',
  frameworkPath: require.resolve('protractor-cucumber-framework'),
  cucumberOpts: {
    require: [
      testPath + '/env.ts',
      testPath + '/**/*.steps.ts'
    ],
    format: [
      'pretty',
      'json:' + cucumberJsonPath
    ]
  },
  onPrepare: function () {
    browser.manage().window().setSize(1280, 720);
  },
  useAllAngular2AppRoots: true,
  beforeLaunch: function () {
    require('ts-node').register({
      project: 'e2e'
    });
    // ensure we have empty directory to store cucumber reports
    fse.emptyDirSync(reportsPath);
  },
  afterLaunch: function () {
    // see docs https://www.npmjs.com/package/cucumber-html-reporter
    let options = {
      theme: 'bootstrap',
      jsonFile: cucumberJsonPath,
      output: cucumberHtmlPath,
      reportSuiteAsScenarios: true,
      storeScreenShots: false,
      launchReport: false,
      metadata: {
        "Browser": "Chrome",
        "Platform": `${os.platform()}  ${os.release()} (${os.arch()})`,
        "Parallel": "no",
      }
    };
    reporter.generate(options);

  }
};
