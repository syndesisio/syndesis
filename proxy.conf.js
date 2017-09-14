const config = require('./src/config.json');

const defaultApiBase = "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com";

if (!config.apiBase) {
  console.warn("Using default API base: ", defaultApiBase);
}

const proxyCfg = {
  '/api': {
    'target': config.apiBase || defaultApiBase,
    'secure': false,
    'changeOrigin': true,
    'ws': true,
    'headers': {
      'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:4200;proto=https'
    }
  },
  '/auth': {
    'target': config.apiBase || defaultApiBase,
    'secure': false,
    'changeOrigin': true
  },
  '/v2/atlas': {
    'target': config.datamapper.baseMappingServiceUrl || defaultApiBase,
    'secure': false,
    'changeOrigin': true,
    'ws': true,
    'headers': {
      'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:4200;proto=https'
    }
  }
};

module.exports = proxyCfg;
