import * as React from 'react';

/* tslint:disable */
const runtime = require('file-loader?name=atlasmap-runtime.js!@syndesis/atlasmap-assembly/dist/atlasmap/runtime.js');
const polyfills = require('file-loader?name=atlasmap-polyfills.js!@syndesis/atlasmap-assembly/dist/atlasmap/polyfills.js');
const styles = require('file-loader?name=atlasmap-styles.js!@syndesis/atlasmap-assembly/dist/atlasmap/styles.js');
const scripts = require('file-loader?name=atlasmap-scripts.js!@syndesis/atlasmap-assembly/dist/atlasmap/scripts.js');
const vendor = require('file-loader?name=atlasmap-vendor.js!@syndesis/atlasmap-assembly/dist/atlasmap/vendor.js');
const main = require('file-loader?name=atlasmap-main.js!@syndesis/atlasmap-assembly/dist/atlasmap/main.js');
/* tslint:enable*/

export class DataMapper extends React.Component {
  public render() {
    const srcDoc = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Atlasmap</title>
  <base href="/dm">

  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
</head>
<body>
  <app-root></app-root>
  <script type="text/javascript" src="${runtime}"></script>
  <script type="text/javascript" src="${polyfills}"></script>
  <script type="text/javascript" src="${styles}"></script>
  <script type="text/javascript" src="${scripts}"></script>
  <script type="text/javascript" src="${vendor}"></script>
  <script type="text/javascript" src="${main}"></script></body>
</html>

`;
    return (
      <div style={{ display: 'flex', flexFlow: 'column', height: '100%' }}>
        <iframe
          srcDoc={srcDoc}
          style={{ width: '100%', height: '100%' }}
          frameBorder={0}
        />
      </div>
    );
  }
}
