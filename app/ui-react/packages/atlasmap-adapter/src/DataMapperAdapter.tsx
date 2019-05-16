import {
  IDocumentProps,
  IInitMessagePayload,
  IMappingsMessagePayload,
} from '@syndesis/atlasmap-assembly/src/app/app.component';
import * as React from 'react';
import equal from 'react-fast-compare';

/* tslint:disable */
const runtime = require('file-loader?name=atlasmap-runtime.js!@syndesis/atlasmap-assembly/dist/atlasmap/runtime.js');
const polyfills = require('file-loader?name=atlasmap-polyfills.js!@syndesis/atlasmap-assembly/dist/atlasmap/polyfills.js');
const styles = require('file-loader?name=atlasmap-styles.js!@syndesis/atlasmap-assembly/dist/atlasmap/styles.js');
const scripts = require('file-loader?name=atlasmap-scripts.js!@syndesis/atlasmap-assembly/dist/atlasmap/scripts.js');
const vendor = require('file-loader?name=atlasmap-vendor.js!@syndesis/atlasmap-assembly/dist/atlasmap/vendor.js');
const main = require('file-loader?name=atlasmap-main.js!@syndesis/atlasmap-assembly/dist/atlasmap/main.js');
/* tslint:enable*/

export enum DocumentType {
  JAVA = 'JAVA',
  JAVA_ARCHIVE = 'JAR',
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

// tslint:disable-next-line
export interface IDocument extends IDocumentProps {}

export interface IDataMapperAdapterProps extends IInitMessagePayload {
  onMappings(mappings: string): void;
}

export class DataMapperAdapter extends React.Component<
  IDataMapperAdapterProps
> {
  protected messageChannel: MessageChannel;
  protected messagePort?: MessagePort;
  protected iframe: HTMLIFrameElement | null = null;

  constructor(props: IDataMapperAdapterProps) {
    super(props);
    this.messageChannel = new MessageChannel();
    this.onIframeLoad = this.onIframeLoad.bind(this);
    this.onMessages = this.onMessages.bind(this);
  }

  public componentDidMount() {
    if (this.iframe) {
      this.iframe.addEventListener('load', this.onIframeLoad);
    }
  }

  public componentWillReceiveProps(nextProps: IDataMapperAdapterProps) {
    const { onMappings: _, ...prevPayload } = this.props;
    const { onMappings: __, ...nextPayload } = nextProps;
    if (!equal(prevPayload, nextPayload)) {
      this.updateDataMapperApp(nextPayload);
    }
  }

  public shouldComponentUpdate() {
    return false;
  }

  public componentWillUnmount() {
    if (this.iframe) {
      this.iframe.removeEventListener('load', this.onIframeLoad);
    }
  }

  public onIframeLoad() {
    this.messagePort = this.messageChannel.port1;
    this.messagePort.onmessage = this.onMessages;
    this.iframe!.contentWindow!.postMessage('init', '*', [
      this.messageChannel.port2,
    ]);
  }

  public onMessages(event: MessageEvent) {
    switch (event.data.message) {
      case 'ready': {
        const { onMappings, ...payload } = this.props;
        this.updateDataMapperApp(payload);
        break;
      }
      case 'mappings': {
        const payload: IMappingsMessagePayload = event.data.payload;
        this.props.onMappings(payload.mappings);
        break;
      }
    }
  }

  public updateDataMapperApp(payload: IInitMessagePayload) {
    if (this.messagePort) {
      const message = {
        message: 'update',
        payload,
      };
      this.messagePort.postMessage(message);
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
<body style="background: transparent;">
  <app-root></app-root>
  <script type="text/javascript" src="${runtime}"></script>
  <script type="text/javascript" src="${polyfills}"></script>
  <script type="text/javascript" src="${styles}"></script>
  <script type="text/javascript" src="${scripts}"></script>
  <script type="text/javascript" src="${vendor}"></script>
  <script type="text/javascript" src="${main}"></script></body>
</html>

`;
    // bypass some odd typing incompatibility with base React when using "exotic"
    // html attributes
    const extraProps = {
      allowtransparency: '',
    };
    return (
      <iframe
        name={'atlasmap-frame'}
        srcDoc={srcDoc}
        style={{ width: '100%', height: '100%', lineHeight: '0' }}
        frameBorder={0}
        {...extraProps}
        ref={el => (this.iframe = el)}
      />
    );
  }
}
