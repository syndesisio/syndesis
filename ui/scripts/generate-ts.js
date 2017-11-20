/*
  Generates a big typescript file from the syndesis swagger json

  Fetch the swagger json from the browser via:

  $.get('/api/v1/swagger.json');

  or for new stuff:

  $.get('/api/v1beta1/swagger.json');

  Copy the swagger into 'swagger.json' in this directory

  Run `yarn genearate` and pull out what you need from _model.ts into model.ts either add or overlay existing models

*/
const https = require("https");
const path = require("path");
const fs = require("fs");
const argv = require("process").argv;
const env = require("process").env;
const url = require("url");
const config = require("../src/config.json");
const CodeGen = require("swagger-js-codegen").CodeGen;
const pluralize = require("pluralize");
const templates = path.join(__dirname, "templates");

const swagger = require("./swagger.json");
const outputFile = "src/app/_model.ts";

const tsSourceCode = CodeGen.getTypescriptCode({
  className: "SyndesisRest",
  swagger: swagger,
  template: {
    class: fs.readFileSync(
      path.join(templates, "typescript-class.mustache"),
      "utf-8"
    ),
    method: fs.readFileSync(
      path.join(templates, "typescript-method.mustache"),
      "utf-8"
    ),
    type: fs.readFileSync(path.join(templates, "type.mustache"), "utf-8")
  },
  mustache: {
    pluralize: function() {
      return function(text, render) {
        return pluralize(render(text));
      };
    }
  }
});
fs.writeFileSync(outputFile, tsSourceCode);
console.log("Wrote file: ", outputFile);
