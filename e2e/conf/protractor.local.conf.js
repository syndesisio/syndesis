// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

let merge = require('merge');

// load base config
let baseConfig = require('./protractor.base.conf').config;


// changes specific to local testing
exports.config = merge(baseConfig, {

  baseUrl: 'http://localhost:4200/',

});
