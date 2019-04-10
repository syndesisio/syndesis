const config = require('./src/config.json');

const defaultApiBase =
  'https://syndesis-staging.b6ff.rh-idev.openshiftapps.com';

const cookie = (process.env.BACKEND_COOKIE || '').trim();
const backend =
  process.env.BACKEND || config.backendBase || config.apiBase || defaultApiBase;

console.log('Using API base: ', backend);

const proxyCfg = {
  '/api': {
    target: backend,
    secure: false,
    changeOrigin: true,
    ws: true,
    headers: {
      'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:4200;proto=https',
      'X-Forwarded-User': 'user',
      'X-Forwarded-Access-Token': 'supersecret',
      Cookie: cookie,
    },
    logLevel: 'debug',
  },
  '/auth': {
    target: backend,
    secure: false,
    changeOrigin: true,
  },
  '/v2/atlas': {
    target: config.datamapper.baseMappingServiceUrl || backend,
    secure: false,
    changeOrigin: true,
    ws: true,
    headers: {
      'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:4200;proto=https',
    },
  },
};

module.exports = proxyCfg;
