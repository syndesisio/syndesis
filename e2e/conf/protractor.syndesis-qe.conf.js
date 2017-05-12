// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

let merge = require('merge');

// load base config
let baseConfig = require('./protractor.base.conf').config;

// ensure we have ui url defined
let syndesisUrl = process.env.SYNDESIS_UI_URL || null;
if (syndesisUrl === null) {
  throw new Error("You must specify shell env SYNDESIS_UI_URL");
}
console.log(`Using syndesis ui on url ${syndesisUrl}`);

// changes specific to local testing
exports.config = merge(baseConfig, {

  baseUrl: syndesisUrl,

});
