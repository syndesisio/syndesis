const fs = require('fs');
const path = require('path');
const jsonfile = require('jsonfile');
const jq = require('node-jq')
const readDir = require('readdir');
const SVGO = require('svgo');

const DEPLOYMENT_JSON = '../../../app/server/dao/src/main/resources/io/syndesis/server/dao/deployment.json';
const CONNECTORS_PATH = '../../../app/connector';
const SCHEMA_JAVA = '../../../app/common/model/src/main/java/io/syndesis/common/model/Schema.java';
const STEP_ICON_ASSET_PATH = ['../../../app/ui-react/syndesis/public/icons/steps'];
const CONNECTOR_ICON_ASSET_PATH = ['../../../app/ui-react/syndesis/public/icons'];

const MAPPINGS = jsonfile.readFileSync('mapping.json');

const svgo = new SVGO({
  quiet: true
});

function optimize(file) {
  const data = fs.readFileSync(file);
  return svgo.optimize(data).then(optimized => {
    const mapping = MAPPINGS.find(c => c.icon == file);
    const id = mapping.connectorId || mapping.stepId;
    return {
      id: id,
      name: path.basename(file),
      iconFile: id + ".svg",
      icon: 'assets:' + id + ".svg",
      raw: optimized.data
    };
  });
}

const svgs = readDir.readSync('.', ['*.svg']);

const missingSvgs = MAPPINGS.map(c => c.icon).filter(svg => svgs.indexOf(svg) < 0);
if (missingSvgs.length != 0) {
  console.log("No SVG icon found for:");
  missingSvgs.forEach(e => console.log(e));
  return;
}

const missingMappings = svgs.filter(file => !(MAPPINGS.find(c => c.icon == file)));
if (missingMappings.length != 0) {
  console.log("No mapping.json entry found for:");
  missingMappings.forEach(e => console.log(e));
  return;
}

Promise.all(MAPPINGS.filter(c => !c.ignore).map(c => {
  return optimize(c.icon);
})).then(optimized => {
  jsonfile.readFile(DEPLOYMENT_JSON)
    .then(deployment => {
      let madeUpdates = false;

      deployment.forEach(obj => {
        if (!(obj.data.connectorId)) {
          return;
        }
        const update = optimized.find(o => o.id == obj.data.connectorId);
        if (update && obj.data.icon !== update.icon) {
          obj.data.icon = update.icon;
          madeUpdates = true;
        }
      })

      if (madeUpdates) {
        jsonfile.writeFile(DEPLOYMENT_JSON, deployment).then(
          () => jq.run('.', DEPLOYMENT_JSON, { sort: true }).then(out => fs.writeFileSync(DEPLOYMENT_JSON, out + '\n'))
        )
        let schemaJava = fs.readFileSync(SCHEMA_JAVA).toString();
        schemaJava = schemaJava.replace(/VERSION = (\d+);/, (match, version) => 'VERSION = ' + (parseInt(version, 10) + 1) + ';');
        fs.writeFileSync(SCHEMA_JAVA, schemaJava);
      }
    })
    .catch(error => console.error(error))

  readDir.readSync(CONNECTORS_PATH, ['**/*.json'])
    .filter(f => f.indexOf('src/main/resources/META-INF/syndesis/connector') > 0)
    .forEach(f => {
      const descriptorFile = path.join(CONNECTORS_PATH, f);
      jsonfile.readFile(descriptorFile)
        .then(descriptor => {
          const update = optimized.find(o => o.id == descriptor.id);
          if (update) {
            descriptor.icon = update.icon;
          }
          jsonfile.writeFile(descriptorFile, descriptor).then(
            () => jq.run('.', descriptorFile, { sort: true }).then(out => fs.writeFileSync(descriptorFile, out + '\n'))
          )
        })
    });

    STEP_ICON_ASSET_PATH.forEach(p => {
        optimized.filter(o => /^step/.test(o.name))
            .forEach(o => fs.writeFileSync(path.join(p, o.name.replace(/[^_]+_/, '')), o.raw));
    });

    CONNECTOR_ICON_ASSET_PATH.forEach(p => {
        optimized.filter(o => /^(?!step)/.test(o.name))
            .forEach(o => fs.writeFileSync(path.join(p, o.iconFile), o.raw));
    });
});
