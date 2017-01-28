const COVERAGE_OUTPUT_DIR = process.env.CIRCLE_ARTIFACTS || '.';
const REPORT_OUTPUT_DIR = process.env.CIRCLE_TEST_REPORTS || '.';

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', 'angular-cli'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-remap-istanbul'),
      require('angular-cli/plugins/karma'),
      require('karma-junit-reporter'),
    ],
    files: [
      { pattern: './src/test.ts', watched: false },
      './node_modules/jquery/dist/jquery.min.js',
      './node_modules/jquery-match-height/dist/jquery.matchHeight-min.js'
    ],
    preprocessors: {
      './src/test.ts': ['angular-cli']
    },
    mime: {
      'text/x-typescript': ['ts', 'tsx']
    },
    remapIstanbulReporter: {
      reports: {
        html: COVERAGE_OUTPUT_DIR + '/coverage',
        lcovonly: COVERAGE_OUTPUT_DIR + '/coverage/coverage.lcov',
      }
    },
    junitReporter: {
      outputDir: REPORT_OUTPUT_DIR + '/junit'
    },
    angularCli: {
      config: './angular-cli.json',
      environment: 'dev'
    },
    reporters: config.angularCli && config.angularCli.codeCoverage
      ? ['progress', 'karma-remap-istanbul', 'junit']
      : ['progress'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome_no_sandbox_disable_gpu'],
    customLaunchers: {
      Chrome_no_sandbox_disable_gpu: {
        base: 'Chrome',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    singleRun: false
  });
};
