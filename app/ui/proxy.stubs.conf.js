const config = require('./src/config.json');
const http = require('http');

const defaultApiBase = "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com";

if (!config.apiBase) {
  console.warn("Using default API base: ", defaultApiBase);
}

const apiBase = config.apiBase || defaultApiBase;

const req = http.request({
  port: 10080,
  method: 'POST',
  path: '/__admin/mappings',
});

req.on('error', (e) => {
  console.error(`Unable to create default proxy mapping: ${e.message}`);
});

req.write(`{
  "id" : "c563e917-910a-4e2f-878a-c9f64999e873",
  "request" : {
    "method" : "ANY"
  },
  "response" : {
    "status" : 200,
    "proxyBaseUrl" : "${apiBase}"
  },
  "uuid" : "c562e917-910a-4e2f-878a-c9f64999e873",
  "priority" : 10
}`);
req.end();

const proxyCfg = {
  '/api': {
    'target': 'http://localhost:10080',
    'secure': false,
    'changeOrigin': true
  },
  '/auth': {
    'target': apiBase,
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
