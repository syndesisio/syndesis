const config = require('./src/config.json');

const proxyCfg = {
  '/api': {
    'target': config.apiBase,
    'secure': false,
    'changeOrigin': true,
    'ws': true,
    'headers': {
      'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:4200;proto=https'
    }
  },
  '/auth': {
    'target': config.apiBase,
    'secure': false,
    'changeOrigin': true
  }
};

module.exports = proxyCfg;
