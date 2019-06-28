/* tslint:disable */
const proxy = require('http-proxy-middleware');
const path = require('path');

module.exports = function(app) {
  if (process.env.BACKEND) {
    ['/api/v1', '/vdb-builder/v1'].forEach(url => 
      app.use( proxy(url, { 
        target: process.env.BACKEND,
        secure: false,
        changeOrigin: true,
        ws: !process.env.PROXY_NO_WS,
        headers: {
          'X-Forwarded-Origin': 'for=127.0.0.1;host=localhost:3000;proto=https',
          'X-Forwarded-Access-Token': 'supersecret',
          Cookie: process.env.BACKEND_COOKIE || '',
        },
      })
    ));
    app.use('/logout', (req, res) => {
      res.clearCookie('_oauth_proxy', {
        domain: req.headers.referrer,
        path: '/',
      });
      res.sendFile(path.join(__dirname, '..', '/public/logout.html'));
    });
  }
};
