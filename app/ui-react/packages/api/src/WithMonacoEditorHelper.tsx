import * as monaco from 'monaco-editor-core';
import {
  CloseAction,
  createConnection,
  ErrorAction,
  MonacoLanguageClient,
  MonacoServices,
} from 'monaco-languageclient';
import * as React from 'react';
import { listen, MessageConnection } from 'vscode-ws-jsonrpc';

export const LANGUAGE_SERVICE_CLIENT_CONNECTED = 'connected';

export interface IWithMonacoEditorHelperProps {
  dvApiUri: string;
  children(props: IWithMonacoEditorHelperChildrenProps): any;
}

export interface IWithMonacoEditorHelperChildrenProps {
  didMountEditor: (valueGetter: any, editor: any) => void;
  willMountEditor: () => void;
}

const LANGUAGE_ID = 'sql'; // 'teiid-ddl';
const LANGUAGE_SERVER_ID = 'teiid-ddl-language-server';
let webSocket: WebSocket;

export class WithMonacoEditorHelper extends React.Component<
  IWithMonacoEditorHelperProps
> {

  public state = { editorInstalled: false };

  /*
   * When the text editor has been rendered, we need to create the language server connection and wire
   * it up to a new code mirror adapter
   */
  public handleEditorDidMount = (valueGetter: any, editor: any) => {

    if (!this.state.editorInstalled) {
      editor.codelens = false;

      // ***************************************************************************
      // AFTER the editor is mounted, need to wire the editor to the language server
      // ***************************************************************************

      MonacoServices.install(editor);

      // create the web socket
      // Eclipse launched test web service:  'ws://localhost:8025/teiid-ddl-language-server';
      // Target URL should look like this:  'wss://syndesis-syndesis.nip.io.192.168.42.99.nip.io/dv/teiid-ddl-language-server';

      const baseUrl = this.props.dvApiUri;
      let url = '';

      if (baseUrl.indexOf('https://') > -1) {
        url = baseUrl.replace('https://', 'wss://').replace('/v1', '') + LANGUAGE_SERVER_ID;
      } else {
        url = baseUrl.replace('http://', 'ws://').replace('/v1', '') + LANGUAGE_SERVER_ID;
      }

      webSocket = new WebSocket(url, []);

      // listen when the web socket is opened
      listen({
        webSocket,
        // tslint:disable-next-line:object-literal-sort-keys
        onConnection: connection => {
          // create and start the language client
          const languageClient = this.createLanguageClient(connection);
          const disposable = languageClient.start();
          connection.onClose(() => disposable.dispose());
        },
      });
      this.setState({ editorInstalled: true });
    }
  };

  public createLanguageClient = (connection: MessageConnection) => {
    return new MonacoLanguageClient({
      name: 'Monaco Language Client',
      // tslint:disable-next-line:object-literal-sort-keys
      clientOptions: {
        // use a language id as a document selector
        documentSelector: [LANGUAGE_ID],
        // disable the default error handler
        errorHandler: {
          closed: () => CloseAction.Restart,
          error: () => ErrorAction.Continue,
        },
      },
      // create a language client connection from the JSON RPC connection on demand
      connectionProvider: {
        get: (errorHandler, closeHandler) => {
          return Promise.resolve(
            createConnection(connection, errorHandler, closeHandler)
          );
        },
      },
    });
  };

  /*
   * When the text editor has been rendered, we need to create the language server connection and wire
   * it up to a new code mirror adapter
   */
  public handleEditorWillMount = () => {
    monaco.languages.register({
      extensions: ['.ddl'],
      id: LANGUAGE_ID,
    });
  };

  public render() {
    return this.props.children({
      didMountEditor: this.handleEditorDidMount,
      willMountEditor: this.handleEditorWillMount,
    });
  }
}
