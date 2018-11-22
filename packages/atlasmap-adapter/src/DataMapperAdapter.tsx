import * as React from 'react';

/* tslint:disable */
const runtime = require('file-loader?name=atlasmap-runtime.js!@syndesis/atlasmap-assembly/dist/atlasmap/runtime.js');
const polyfills = require('file-loader?name=atlasmap-polyfills.js!@syndesis/atlasmap-assembly/dist/atlasmap/polyfills.js');
const styles = require('file-loader?name=atlasmap-styles.js!@syndesis/atlasmap-assembly/dist/atlasmap/styles.js');
const scripts = require('file-loader?name=atlasmap-scripts.js!@syndesis/atlasmap-assembly/dist/atlasmap/scripts.js');
const vendor = require('file-loader?name=atlasmap-vendor.js!@syndesis/atlasmap-assembly/dist/atlasmap/vendor.js');
const main = require('file-loader?name=atlasmap-main.js!@syndesis/atlasmap-assembly/dist/atlasmap/main.js');
/* tslint:enable*/

type DocumentType =
  | 'Java'
  | 'XML'
  | 'XSD'
  | 'JSON'
  | 'Core'
  | 'CSV'
  | 'Constants'
  | 'Property';
type InspectionType = 'JAVA_CLASS' | 'SCHEMA' | 'INSTANCE' | 'UNKNOWN';

export interface IDataMapperAdapterProps {
  inputDataShape: string;
  inputDescription: string;
  inputDocumentType: DocumentType;
  inputId: string;
  inputInspectionType: InspectionType;
  inputName: string;
  outputDataShape: string;
  outputDescription: string;
  outputDocumentType: DocumentType;
  outputId: string;
  outputInspectionType: InspectionType;
  outputName: string;
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
          inputDataShape: this.props.inputDataShape,
          inputDescription: this.props.inputDescription,
          inputDocumentType: this.props.inputDocumentType,
          inputId: this.props.inputId,
          inputInspectionType: this.props.inputInspectionType,
          inputName: this.props.inputName,
          outputDataShape: this.props.outputDataShape,
          outputDescription: this.props.outputDescription,
          outputDocumentType: this.props.outputDocumentType,
          outputId: this.props.outputId,
          outputInspectionType: this.props.outputInspectionType,
          outputName: this.props.outputName,
        },
      },
      '*',
      [this.messageChannel.port2]
    );
  }

  public onMessage(event: MessageEvent) {
    // TODO
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
