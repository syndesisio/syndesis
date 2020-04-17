import {
  Alert,
  AlertActionCloseButton,
  Button,
  ButtonVariant,
  Card,
  CardBody,
  CardFooter,
  Grid,
  GridItem,
  Text,
  TextContent,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { ConnectionTreeComponent } from '.';
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
  i18nCursorColumn: string;
  i18nCursorLine: string;
  i18nDdlTextPlaceholder: string;
  i18nDoneLabel: string;
  i18nSaveLabel: string;
  i18nTitle: string;
  i18nMetadataTitle: string;
  i18nLoading: string;
  previewExpanded: boolean;
  i18nValidationResultsTitle: string;
  /**
   * `true` if the validation message is to be shown
   */
  showValidationMessage: boolean;
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
   * Unformatted Source info
   */
  sourceInfo: any;
  onCloseValidationMessage: () => void;
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

const compPropsAreEqual = (prevProps: any, nextProps: any) => {
  return (
    prevProps.sourceTableInfos === nextProps.sourceTableInfos &&
    prevProps.sourceInfo === nextProps.sourceInfo &&
    prevProps.viewDdl === nextProps.viewDdl &&
    prevProps.previewExpanded === nextProps.previewExpanded &&
    prevProps.showValidationMessage === nextProps.showValidationMessage &&
    prevProps.isSaving === nextProps.isSaving &&
    prevProps.validationResults === nextProps.validationResults
  );
};

export const DdlEditor: React.FunctionComponent<IDdlEditorProps> = React.memo(
  props => {
    const [ddlValue, setDdlValue] = React.useState(props.viewDdl);
    const [initialDdlValue] = React.useState(props.viewDdl);
    const [hasChanges, setHasChanges] = React.useState(false);
    const [savedValue, setSavedValue] = React.useState(props.viewDdl);
    const [keywordsRegistered, setKeywordsRegistered] = React.useState(false);
    const [cursorPosition, setCursorPosition] = React.useState(
      `( ${props.i18nCursorLine} ?, ${props.i18nCursorColumn} ? )`
    );
    const getMetadataTree = (sourceInfo: any): Map<string, any> => {
      const treeInfo = new Map<string, any>();

      for (const connection of sourceInfo) {
        treeInfo.set(connection.name, connection.tables);
      }
      return treeInfo;
    };

    const handleCloseValidationMessage = () => {
      props.onCloseValidationMessage();
    };

    const handleEditorDidMount = (editor: ITextEditor) => {
      editor.on('cursorActivity', cm => {
        const pos = editor.getCursor();
        setCursorPosition(getCursorText(pos));
      });
    };

    const getCursorText = (pos: any) => {
      return `( ${props.i18nCursorLine} ${pos.line + 1}, ${
        props.i18nCursorColumn
      } ${pos.ch + 1} )`;
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
      autoCloseBrackets: true,
      autofocus: true,
      extraKeys: { 'Ctrl-Space': 'autocomplete' },
      gutters: ['CodeMirror-lint-markers'],
      hintOptions: getHintOptions(props.sourceTableInfos),
      indentWithTabs: true,
      lineNumbers: true,
      lineWrapping: true,
      matchBrackets: true,
      mode: dvLanguageMode,
      placeholder: props.i18nDdlTextPlaceholder,
      readOnly: false,
      showCursorWhenSelecting: true,
      styleActiveLine: true,
      tabSize: 2,
    };

    const memoisedValue = React.useMemo(
      () => getMetadataTree(props.sourceInfo),
      [props.sourceInfo]
    );

    return (
      <Grid style={{ flexGrow: 1 }}>
        <GridItem span={3}>
          <PageSection
            isFilled={true}
            variant={'light'}
            className={'ddl-editor'}
          >
            <Title headingLevel="h5" size="lg">
              {props.i18nMetadataTitle}
            </Title>
            <div
              className={
                props.previewExpanded
                  ? 'ddl-editor_metatree_table ddl-editor_metatree_table_collapsed'
                  : 'ddl-editor_metatree_table ddl-editor_metatree_table_expanded'
              }
            >
              <ConnectionTreeComponent
                metadataTree={memoisedValue}
                i18nLoading={props.i18nLoading}
              />
            </div>
          </PageSection>
        </GridItem>
        <GridItem span={9}>
          <PageSection
            isFilled={true}
            variant={'light'}
            className={'ddl-editor'}
          >
            <Title headingLevel="h5" size="lg">
              {props.i18nTitle}
            </Title>
            {props.showValidationMessage
              ? props.validationResults.map((e, idx) => (
                  <Alert
                    key={idx}
                    variant={e.type}
                    isInline={true}
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
            <Card>
              <CardBody className={'ddl-editor__card-body'}>
                <TextEditor
                  value={initialDdlValue}
                  options={editorOptions}
                  onChange={handleDdlChange}
                  editorDidMount={handleEditorDidMount}
                />
              </CardBody>
              <CardFooter className={'ddl-editor__card-footer'}>
                <Button
                  data-testid={'ddl-editor-done-button'}
                  className="ddl-editor__button"
                  isDisabled={props.isSaving}
                  variant={ButtonVariant.secondary}
                  onClick={handleFinish}
                >
                  {props.i18nDoneLabel}
                </Button>
                <Button
                  data-testid={'ddl-editor-save-button'}
                  className="ddl-editor__button"
                  isDisabled={props.isSaving || !hasChanges}
                  variant={ButtonVariant.primary}
                  onClick={handleSave}
                >
                  {props.isSaving ? <Loader size={'xs'} inline={true} /> : null}
                  {props.i18nSaveLabel}
                </Button>
              </CardFooter>
            </Card>
          </PageSection>
        </GridItem>
      </Grid>
    );
  },
  compPropsAreEqual
);
