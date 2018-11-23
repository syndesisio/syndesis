import {
  IInitMessagePayload,
  IMappingsMessagePayload,
} from '@syndesis/atlasmap-assembly/src/app/app.component';
import * as React from 'react';

/* tslint:disable */
const runtime = require('file-loader?name=atlasmap-runtime.js!@syndesis/atlasmap-assembly/dist/atlasmap/runtime.js');
const polyfills = require('file-loader?name=atlasmap-polyfills.js!@syndesis/atlasmap-assembly/dist/atlasmap/polyfills.js');
const styles = require('file-loader?name=atlasmap-styles.js!@syndesis/atlasmap-assembly/dist/atlasmap/styles.js');
const scripts = require('file-loader?name=atlasmap-scripts.js!@syndesis/atlasmap-assembly/dist/atlasmap/scripts.js');
const vendor = require('file-loader?name=atlasmap-vendor.js!@syndesis/atlasmap-assembly/dist/atlasmap/vendor.js');
const main = require('file-loader?name=atlasmap-main.js!@syndesis/atlasmap-assembly/dist/atlasmap/main.js');
/* tslint:enable*/

export enum DocumentType {
  JAVA = 'Java',
  XML = 'XML',
  XSD = 'XSD',
  JSON = 'JSON',
  CORE = 'Core',
  CSV = 'CSV',
  CONSTANT = 'Constants',
  PROPERTY = 'Property',
}
export enum InspectionType {
  JAVA_CLASS = 'JAVA_CLASS',
  SCHEMA = 'SCHEMA',
  INSTANCE = 'INSTANCE',
  UNKNOWN = 'UNKNOWN',
}

export interface IDataMapperAdapterProps extends IInitMessagePayload {
  onMappings(mappings: string): void;
}

export class DataMapperAdapter extends React.Component<
  IDataMapperAdapterProps
> {
  protected messageChannel = new MessageChannel();
  protected iframe: HTMLIFrameElement | null = null;

  constructor(props: IDataMapperAdapterProps) {
    super(props);
    this.onIframeLoad = this.onIframeLoad.bind(this);
    this.onMessage = this.onMessage.bind(this);
  }

  public componentDidMount() {
    if (this.iframe) {
      this.iframe.addEventListener('load', this.onIframeLoad);
    }
  }

  public componentWillUnmount() {
    if (this.iframe) {
      this.iframe.removeEventListener('load', this.onIframeLoad);
    }
  }

  public onIframeLoad() {
    this.messageChannel.port1.onmessage = this.onMessage;

    this.iframe!.contentWindow!.postMessage(
      {
        message: 'init',
        payload: {
          documentId: this.props.documentId,
          inputDataShape: this.props.inputDataShape,
          inputDescription: this.props.inputDescription,
          inputDocumentType: this.props.inputDocumentType,
          inputInspectionType: this.props.inputInspectionType,
          inputName: this.props.inputName,
          mappings: this.props.mappings,
          outputDataShape: this.props.outputDataShape,
          outputDescription: this.props.outputDescription,
          outputDocumentType: this.props.outputDocumentType,
          outputInspectionType: this.props.outputInspectionType,
          outputName: this.props.outputName,
        },
      },
      '*',
      [this.messageChannel.port2]
    );
  }

  public onMessage(event: MessageEvent) {
    switch (event.data.message) {
      case 'mappings': {
        const payload: IMappingsMessagePayload = event.data.payload;
        this.props.onMappings(payload.mappings);
      }
    }
  }

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
          ref={el => (this.iframe = el)}
        />
      </div>
    );
  }
}
