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
const snapshotDirPath = path.join(__dirname, '..', 'snapshots');
const snapshotFilePath = path.join(
  snapshotDirPath,
  toJSONLocal() + '-snapshot.json'
);

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
 */
function getLatestFile(dirpath) {
  // Check if directory path exists
  let latest;

  const files = fs.readdirSync(dirpath);
  files.forEach(filename => {
    // Get the file stats
    const stat = fs.lstatSync(path.join(dirpath, filename));
    // Continue if it is a directory
    if (stat.isDirectory()) return;

    // "latest" default to first file
    if (!latest) {
      latest = { filename, mtime: stat.mtime };
      return;
    }
    // Update "latest" if mtime is greater than the current "latest"
    if (stat.mtime > latest.mtime) {
      latest.filename = filename;
      latest.mtime = stat.mtime;
    }
  });

  return latest;
}

module.exports = (on, config) => {
  // `on` is used to hook into various events Cypress emits
  // `config` is the resolved Cypress config
  on('task', {
    getSnapshot() {
      return getLatestFile(snapshotDirPath);
    },

    storeSnapshot(snapshot) {
      const snapshotJson = JSON.stringify(snapshot);
      fs.writeFileSync(snapshotFilePath, snapshotJson, err => {
        if (err) throw err;
        cy.log('The file has been saved!');
      });

      return null;
    },
  });
};
