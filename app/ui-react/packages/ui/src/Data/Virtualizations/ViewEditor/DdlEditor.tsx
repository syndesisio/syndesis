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
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import MonacoEditor from 'react-monaco-editor';
import { ConnectionTreeComponent } from '.';
import { Loader, PageSection } from '../../../Layout';
import './DdlEditor.css';

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
   * The localized text for the MetaData tree title
   */
  i18nMetadataTitle: string;

  /**
   * The localized text for the Loading
   */
  i18nLoading: string;

  /**
   * Preview Data Table state
   */
  previewExpanded: boolean;

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
   * Unformatted Source info
   */
  sourceInfo: any;

  /**
   * The callback for notifying the monaco helper that the editor did mount
   */
  didmount: (valueGetter: any, editor: any) => void;

  /**
   * The callback for notifying the monaco helper that the editor will mount
   */
  willMount:  () => void;

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

const getMetadataTree = (sourceInfo: any): Map<string, any> => {
  const treeInfo = new Map<string, any>();

  for (const connection of sourceInfo) {
    treeInfo.set(connection.name, connection.tables);
  }
  return treeInfo;
};

export const DdlEditor: React.FunctionComponent<IDdlEditorProps> = props => {
  const [ddlValue, setDdlValue] = React.useState(props.viewDdl);
  const [hasChanges, setHasChanges] = React.useState(false);
  const [savedValue, setSavedValue] = React.useState(props.viewDdl);

  const LANGUAGE_ID = 'sql';


  const handleCloseValidationMessage = () => {
    props.onCloseValidationMessage();
  };

  const handleEditorChange = (value: any) => {
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

  const editorOptions = {
    codeLens: false,
    selectOnLineNumbers: true,
    useTabStops: true,
  };

  const metadataTree = getMetadataTree(props.sourceInfo);

  return (
    <Grid style={{ flexGrow: 1 }}>
      <GridItem span={3}>
        <PageSection isFilled={true} variant={'light'} className={'ddl-editor'}>
          <Title headingLevel="h5" size="lg">
            {props.i18nMetadataTitle}
          </Title>
          <div
            className={
              props.previewExpanded
                ? 'ddl-editor_metatree_table ddl-editor_metatree_table_scroll'
                : 'ddl-editor_metatree_table'
            }
          >
            <ConnectionTreeComponent
              metadataTree={metadataTree}
              i18nLoading={props.i18nLoading}
            />
          </div>
        </PageSection>
      </GridItem>
      <GridItem span={9}>
        <PageSection isFilled={true} variant={'light'} className={'ddl-editor'}>
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
          <Card>
            <CardBody className={'ddl-editor__card-body'}>
              <MonacoEditor
                width="100%"
                height="300"
                language={LANGUAGE_ID}
                theme="vs"
                value={ddlValue}
                options={editorOptions}
                onChange={handleEditorChange}
                editorDidMount={props.didmount}
                editorWillMount={props.willMount}
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
};
