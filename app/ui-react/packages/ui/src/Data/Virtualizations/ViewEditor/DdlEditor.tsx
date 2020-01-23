import {
  Alert,
  AlertActionCloseButton,
  Card,
  CardBody,
  CardFooter,
  Text,
  TextContent,
  Title,
} from '@patternfly/react-core';
import * as monaco from 'monaco-editor-core';
import {
  CloseAction,
  createConnection,
  ErrorAction,
  MonacoLanguageClient,
  MonacoServices,
} from 'monaco-languageclient';
import { Button } from 'patternfly-react';
import * as React from 'react';
import { useRef } from 'react';
import MonacoEditor from 'react-monaco-editor';
import { listen, MessageConnection } from 'vscode-ws-jsonrpc';
import { Loader, PageSection } from '../../../Layout';
import { ITextEditor, TextEditor } from '../../../Shared';
import './DdlEditor.css';
import { dvLanguageMode, loadDvMime } from './DvAutocomplete';

export interface IViewEditValidationResult {
  message: string;
  type: 'danger' | 'success';
}

export interface ITableInfo {
  name: string;
  columnNames: string[];
}

export interface IDdlEditorProps {
  viewDdl: string;

  /**
   * The localized text for the cursor Column.
   */
  i18nCursorColumn: string;

  /**
   * The localized text for the cursor Line.
   */
  i18nCursorLine: string;

  /**
   * The localized text for the DDL text placeholder when no content exists.
   */
  i18nDdlTextPlaceholder: string;

  /**
   * The localized text for the done button.
   */
  i18nDoneLabel: string;

  /**
   * The localized text for the save button.
   */
  i18nSaveLabel: string;

  /**
   * The localized text for the title
   */
  i18nTitle: string;

  /**
   * The localized text for the validate results message title
   */
  i18nValidationResultsTitle: string;

  /**
   * `true` if the validation message is to be shown
   */
  showValidationMessage: boolean;

  /**
   * `true` if save is in progress.
   */
  isSaving: boolean;

  /**
   * View validationResults
   */
  validationResults: IViewEditValidationResult[];

  /**
   * Source table-columns for code completion
   */
  sourceTableInfos: ITableInfo[];

  /**
   * The callback for closing the validation message
   */
  onCloseValidationMessage: () => void;

  /**
   * The callback for when the done button is clicked
   */
  onFinish: () => void;

  /**
   * The callback for when the save button is clicked
   * @param ddl the text area ddl
   * @returns `true` if saving the DDL was successful
   */
  onSave: (ddl: string) => Promise<boolean>;

  /**
   * @param dirty `true` if the editor has unsaved changes
   */
  setDirty: (dirty: boolean) => void;
}

export const DdlEditor: React.FunctionComponent<IDdlEditorProps> = props => {
  const [ddlValue, setDdlValue] = React.useState(props.viewDdl);
  const [initialDdlValue] = React.useState(props.viewDdl);
  const [hasChanges, setHasChanges] = React.useState(false);
  const [savedValue, setSavedValue] = React.useState(props.viewDdl);
  const [keywordsRegistered, setKeywordsRegistered] = React.useState(false);
  const [cursorPosition, setCursorPosition] = React.useState(
    `( ${props.i18nCursorLine} ?, ${props.i18nCursorColumn} ? )`
  );
  const currentValueGetter = useRef();
  const editorRef = useRef();
  const LANGUAGE_ID = 'teiid-ddl';

  const handleCloseValidationMessage = () => {
    props.onCloseValidationMessage();
  };

  /*
   * When the text editor has been rendered, we need to create the language server connection and wire
   * it up to a new code mirror adapter
   */
  const handleEditorDidMount = (valueGetter: any, editor: any) => {
    editor.codelens = false;
    currentValueGetter.current = valueGetter;
    editorRef.current = editor;

    // ***************************************************************************
    // AFTER the editor is mounted, need to wire the editor to the language server
    // ***************************************************************************

    // install Monaco language client services
    MonacoServices.install(editor);

    // create the web socket
    const url = 'ws://localhost:8025/teiid-ddl-language-server';
    const webSocket = new WebSocket(url, []);

    // listen when the web socket is opened
    listen({
      webSocket,
      onConnection: connection => {
        // create and start the language client
        const languageClient = createLanguageClient(connection);
        const disposable = languageClient.start();
        connection.onClose(() => disposable.dispose());
      },
    });
  };


  /*
   * When the text editor has been rendered, we need to create the language server connection and wire
   * it up to a new code mirror adapter
   */
  const handleEditorWillMount = () => {
    // tslint:disable-next-line:no-console
    console.log('   >>>  DdlEditor  handleEditorWillMount() called.... = ');

    monaco.languages.register({
      extensions: ['.ddl'],
      id: LANGUAGE_ID,
    });
  };

  const getCursorText = (pos: any) => {
    return `( ${props.i18nCursorLine} ${pos.line + 1}, ${
      props.i18nCursorColumn
    } ${pos.ch + 1} )`;
  };

  const createLanguageClient = (connection: MessageConnection) => {
    return new MonacoLanguageClient({
      name: 'Sample Language Client',
      // tslint:disable-next-line:object-literal-sort-keys
      clientOptions: {
        // use a language id as a document selector
        documentSelector: [LANGUAGE_ID],
        // disable the default error handler
        errorHandler: {
          closed: () => CloseAction.DoNotRestart,
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

  const handleCloseValidationMessage = () => {
    props.onCloseValidationMessage();
  };

  const handleDdlChange = (editor: ITextEditor, data: any, value: string) => {
    setDdlValue(value);
    handleCloseValidationMessage();

    const dirty = value !== savedValue;

    if (dirty !== hasChanges) {
      setHasChanges(dirty);
      props.setDirty(dirty);
    }
  };

  const handleFinish = () => {
    props.onFinish();
  };

  const handleSave = async () => {
    // tslint:disable-next-line:no-console
    console.log('   >>>  DdlEditor  handleSave() called..... ');
    const saved = await props.onSave(ddlValue);
    if (saved) {
      setSavedValue(ddlValue);
      setHasChanges(false);
      props.setDirty(false);
    }
  };

  /**
   * reformats the tableInfo into the format expected by hintOptions
   * Example -
   *   tables: {
   *     countries: ['name', 'population', 'size'],
   *     users: ['name', 'score', 'birthDate'],
   *   }
   * @param tableInfos the table infos
   */
  const getHintOptions = (tableInfos: ITableInfo[]) => {
    if (!keywordsRegistered) {
      loadDvMime();
      setKeywordsRegistered(true);
    }

    const result = { tables: {} };

    for (const tableInfo of tableInfos) {
      result.tables[tableInfo.name] = tableInfo.columnNames;
    }
    return result;
  };

  const editorOptions = {
    codeLens: false,
    selectOnLineNumbers: true,
    useTabStops: true,
  };

  return (
    <PageSection isFilled={true} variant={'light'} className={'ddl-editor'}>
      <Title headingLevel="h5" size="lg">
        {props.i18nTitle}
      </Title>
      {props.showValidationMessage
        ? props.validationResults.map((e, idx) => (
            <Alert
              key={idx}
              variant={e.type}
              title={props.i18nValidationResultsTitle}
              action={
                <AlertActionCloseButton
                  onClose={handleCloseValidationMessage}
                />
              }
            >
              {e.message}
            </Alert>
          ))
        : null}
      <TextContent>
        <Text className={'ddl-editor-cursor-position-text'}>
          {cursorPosition}
        </Text>
      </TextContent>
      <Card className={'ddl-editor__card'}>
        <CardBody className={'ddl-editor__card-body'}>
          {props.showValidationMessage
            ? props.validationResults.map((e, idx) => (
                <Alert
                  key={idx}
                  variant={e.type}
                  title={props.i18nValidationResultsTitle}
                  action={
                    <AlertActionCloseButton
                      onClose={handleCloseValidationMessage}
                    />
                  }
                >
                  {e.message}
                </Alert>
              ))
            : null}
          <MonacoEditor
            width="100%"
            height="300"
            language={LANGUAGE_ID}
            theme="vs"
            value={ddlValue}
            options={editorOptions}
            onChange={handleEditorChange}
            editorDidMount={handleEditorDidMount}
            editorWillMount={handleEditorWillMount}
          />
        </CardBody>
        <CardFooter className={'ddl-editor__card-footer'}>
          <Button
            data-testid={'ddl-editor-done-button'}
            bsStyle="default"
            className="ddl-editor__button"
            disabled={props.isSaving}
            onClick={handleFinish}
          >
            {props.i18nDoneLabel}
          </Button>
          <Button
            data-testid={'ddl-editor-save-button'}
            bsStyle="primary"
            className="ddl-editor__button"
            disabled={props.isSaving || !hasChanges}
            onClick={handleSave}
          >
            {props.isSaving ? <Loader size={'xs'} inline={true} /> : null}
            {props.i18nSaveLabel}
          </Button>
        </CardFooter>
      </Card>
    </PageSection>
  );
};
