let env = process.env.TEST_ENV || 'local';

switch (env) {
  case 'local':
    exports.config = require('./e2e/conf/protractor.local.conf').config;
    break;

  case 'ipaas-qe':
    exports.config = require('./e2e/conf/protractor.ipaas-qe.conf').config;
    break;

  default:
    exports.config = require('./e2e/conf/protractor.local.conf').config;
    break;

}
