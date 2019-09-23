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

/**
 * Offsets for timezone differences
 */
function toJSONLocal() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toJSON().slice(0, 10);
}

const snapshotFilePath = path.join(
  snapshotDirPath,
  toJSONLocal() + '-snapshot.json'
);

/**
 * Util function that retrieves the most recently updated file in a directory,
 * which we are using for snapshots.
 */
function getLatestFilePath(snapshotDirPath) {
  // Check if directory path exists
  let latest;

  const files = fs.readdirSync(snapshotDirPath);
  files.forEach(filename => {
    const filePath = path.join(snapshotDirPath, filename);
    // Get the file stats
    const stat = fs.lstatSync(filePath);
    // Continue if it is a directory
    if (stat.isDirectory()) return;

    // "latest" default to first file
    if (!latest) {
      latest = { filename: filename, mtime: stat.mtime };
      return;
    }
    // Update "latest" if mtime is greater than the current "latest"
    if (stat.mtime > latest.mtime) {
      latest.filename = filename;
      latest.mtime = stat.mtime;
    }
  });

  return path.join(snapshotDirPath, latest.filename);
}

module.exports = (on, config) => {
  // `on` is used to hook into various events Cypress emits
  // `config` is the resolved Cypress config
  on('task', {
    getSnapshot() {
      const latestFilePath = getLatestFilePath(snapshotDirPath);

      if (fs.existsSync(latestFilePath)) {
        return fs.readFileSync(latestFilePath, 'utf8');
      }

      return null;
    },

    storeSnapshot(snapshot) {
      const snapshotJson = JSON.stringify(snapshot);
      fs.writeFileSync(snapshotFilePath, snapshotJson.body, err => {
        if (err) throw err;
        cy.log('The file has been saved!');
      });

      return null;
    },
  });
};
