/* tslint:disable */
const talkback = require('talkback');
const proxy = require('http-proxy-middleware');
const filenamify = require('filenamify');
const config = require('../public/config.json');

// Disable self-signed certificate check
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const backendName = filenamify(process.env.BACKEND);

const server = talkback({
  host: process.env.BACKEND,
  port: 556,
  path: __dirname + '/../tapes/' + backendName,
  record: process.env.MOCKS_RECORD,
  ignoreHeaders: [
    'content-length',
    'host',
    'cookie',
    'cache-control',
    'pragma',
    'x-forwarded-origin',
  ],
  summary: false,
  debug: true,
});
server.start(() => console.log('Talkback Started'));

module.exports = function(app) {
  app.use(
    proxy('/api', {
      target: 'http://localhost:556',
      secure: false,
      changeOrigin: true,
      ws: false,
      headers: {
        'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:9009;proto=https',
        'X-Forwarded-Access-Token': 'supersecret',
        Cookie: process.env.BACKEND_COOKIE || '',
      },
    })
  );
};
