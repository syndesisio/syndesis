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
//const {resolve, join} = require('path');
const repoRoot = path.join(__dirname, '..', '..');
const snapshotPath = 'cypress/snapshots';

/**
 * Offsets for timezone differences
 */
function toJSONLocal() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toJSON().slice(0, 10);
}

/**
 * Util function that retrieves the most recently updated file in a directory,
 * which we are using for snapshots.
 * @param dir
 * @return {{file: *, mtime: *}[]}
 */
function orderRecentFiles(dir) {
  fs.readdir(repoRoot, function(err, list) {
    //console.log('path.join(repoRoot + snapshotPath): ' + path.join(repoRoot + snapshotPath));
    list.forEach(function(file) {
      console.log('file: ' + file);
      let stats = fs.statSync(path.join('.', file));
      console.log('stats.mtime: ' + stats.mtime);
      console.log('stats.ctime: ' + stats.ctime);
      return stats;
    });
  });
}

function getMostRecentFile(dir) {
  const files = orderRecentFiles(dir);
  return files.length ? files[0] : undefined;
}

module.exports = (on, config) => {
  // `on` is used to hook into various events Cypress emits
  // `config` is the resolved Cypress config
  on('task', {
    getSnapshot() {
      console.log('repoRoot: ' + repoRoot);
      return getMostRecentFile(repoRoot);
    },

    test() {
      //console.log(process.env);
      console.log('repoRoot: ' + repoRoot);
      console.log('repoRoot: ' + repoRoot);
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
