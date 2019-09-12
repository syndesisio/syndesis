// ***********************************************************
// This example plugins/index.js can be used to load plugins
//
// You can change the location of this file or turn off loading
// the plugins file with the 'pluginsFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/plugins-guide
// ***********************************************************

// This function is called when a project is opened or re-opened (e.g. due to
// the project's config changing)
const fs = require('fs');
const path = require('path');
const repoRoot = path.join(__dirname, '..', '..');

/**
 * Offsets for timezone differences
 */
function toJSONLocal() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toJSON().slice(0, 10);
}

module.exports = (on, config) => {
  // `on` is used to hook into various events Cypress emits
  // `config` is the resolved Cypress config
  on('task', {
    test() {
      console.log(process.env);
      return null;
    },

    storeSnapshot(snapshot) {
      const snapshotJson = JSON.stringify(snapshot);
      fs.writeFileSync(toJSONLocal() + '-snapshot.json', snapshotJson, err => {
        if (err) throw err;
        cy.log('The file has been saved!');
      });

      return null;
    },
  });
};
