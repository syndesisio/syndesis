const https = require('https');
const path = require('path');
const fs = require('fs');
const argv = require('process').argv;
const env = require('process').env;
const url = require('url');
const config = require('../src/config.json');
const CodeGen = require('swagger-js-codegen').CodeGen;
const templates = path.join(__dirname, 'templates');
const swaggerUrl = 'https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/api/v1/swagger.json';
const outputFile = 'src/app/model.ts';
console.log("Fetching: ", swaggerUrl);
https.get(swaggerUrl, (response) => {
  //console.log("Response: ", response);
  var body = '';
  response.on('data', (data) => {
    body += data;
  })
  response.on('end', () => {
    try {
      const swagger = JSON.parse(body);
      const tsSourceCode = CodeGen.getTypescriptCode({
        className: 'SyndesisRest',
        swagger: swagger,
        template: {
          class: fs.readFileSync(path.join(templates, 'typescript-class.mustache'), 'utf-8'),
          method: fs.readFileSync(path.join(templates, 'typescript-method.mustache'), 'utf-8'),
          type: fs.readFileSync(path.join(templates, 'type.mustache'), 'utf-8'),
        },
      });
      fs.writeFileSync(outputFile, tsSourceCode);
      console.log("Wrote file: ", outputFile);
    } catch (err) {
      console.log("Failed to generate typescript: ", err);
    }
  });
});
