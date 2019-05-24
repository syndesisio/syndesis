import {
  ApiDefinition,
  IApiDefinitionPayload,
  IInitPayload,
} from '@syndesis/apicurio-assembly/src/app/app.component';
import * as React from 'react';
import equal from 'react-fast-compare';

/* tslint:disable */
const runtime = require('file-loader?name=apicurio-runtime.js!@syndesis/apicurio-assembly/dist/apicurio/runtime.js');
const polyfills = require('file-loader?name=apicurio-polyfills.js!@syndesis/apicurio-assembly/dist/apicurio/polyfills.js');
const styles = require('file-loader?name=apicurio-styles.js!@syndesis/apicurio-assembly/dist/apicurio/styles.js');
const scripts = require('file-loader?name=apicurio-scripts.js!@syndesis/apicurio-assembly/dist/apicurio/scripts.js');
const vendor = require('file-loader?name=apicurio-vendor.js!@syndesis/apicurio-assembly/dist/apicurio/vendor.js');
const main = require('file-loader?name=apicurio-main.js!@syndesis/apicurio-assembly/dist/apicurio/main.js');
/* tslint:enable*/

export interface IApicurioAdapterProps extends IInitPayload {
  onSpecification(specification: ApiDefinition): void;
}

export class ApicurioAdapter extends React.Component<IApicurioAdapterProps> {
  protected messageChannel: MessageChannel;
  protected messagePort?: MessagePort;
  protected iframe: HTMLIFrameElement | null = null;

  constructor(props: IApicurioAdapterProps) {
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

  public componentWillReceiveProps(nextProps: IApicurioAdapterProps) {
    const { onSpecification: _, ...prevPayload } = this.props;
    const { onSpecification: __, ...nextPayload } = nextProps;
    if (!equal(prevPayload, nextPayload)) {
      this.updateApicurioApp(nextPayload);
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
        const { onSpecification, ...payload } = this.props;
        this.updateApicurioApp(payload);
        break;
      }
      case 'specification': {
        const payload: IApiDefinitionPayload = event.data.payload;
        this.props.onSpecification(payload.specification);
        break;
      }
    }
  }

  public updateApicurioApp(payload: IInitPayload) {
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
  <title>Apicurio</title>
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
        name={'apicurio-frame'}
        srcDoc={srcDoc}
        style={{ width: '100%', height: '100%', lineHeight: '0' }}
        frameBorder={0}
        {...extraProps}
        ref={el => (this.iframe = el)}
      />
    );
  }
}
