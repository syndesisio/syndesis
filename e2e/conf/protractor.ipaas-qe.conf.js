// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

let merge = require('merge');

// load base config
let baseConfig = require('./protractor.base.conf').config;

// ensure we have ui url defined
let ipaasUrl = process.env.IPAAS_UI_URL || null;
if (ipaasUrl === null) {
  throw new Error("You must specify shell env IPAAS_UI_URL");
}
console.log(`Using ipaas ui on url ${ipaasUrl}`);

// changes specific to local testing
exports.config = merge(baseConfig, {

  baseUrl: ipaasUrl,

});
