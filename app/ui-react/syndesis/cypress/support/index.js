// ***********************************************************
// This example support/index.js is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

import { fetch } from 'whatwg-fetch';

// Import commands.js using ES2015 syntax:
import './commands';

// Alternatively you can use CommonJS syntax:
// require('./commands')

Cypress.on('window:before:load', win => {
  let requestNumber = 0;
  const originalFetch = win.fetch;
  win.fetch = function(url, options) {
    if (options && options.headers) {
      requestNumber += 1;
      options.headers['syndesis-mock-session'] = Cypress.spec.name;
      options.headers['syndesis-mock-request'] = requestNumber;
    }
    console.log(url, options);
    return originalFetch(url, options);
  };
});

Cypress.Screenshot.defaults({
  screenshotOnRunFailure: false,
});
