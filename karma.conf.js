module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular/cli'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-coverage-istanbul-reporter'),
      require('@angular/cli/plugins/karma'),
      require('karma-junit-reporter'),
      require('karma-mocha-reporter')
    ],
    files: [
      { pattern: './src/test.ts', watched: false },
      './node_modules/jquery/dist/jquery.min.js',
      './node_modules/jquery-match-height/dist/jquery.matchHeight-min.js',
      './node_modules/chart.js/dist/Chart.js'
    ],
    preprocessors: {
      './src/test.ts': ['@angular/cli']
    },
    mime: {
      'text/x-typescript': ['ts', 'tsx']
    },
    remapIstanbulReporter: {
      reports: ['html', 'lcovonly'],
      fixWebpackSourcePaths: true
    },
    junitReporter: {
      outputDir: './junit'
    },
    angularCli: {
      config: './angular-cli.json',
      environment: 'dev'
    },
    reporters: config.angularCli && config.angularCli.codeCoverage
      ? ['mocha', 'coverage-istanbul', 'junit']
      : ['mocha'],
    mochaReporter: {
      output: 'full'
    },
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
