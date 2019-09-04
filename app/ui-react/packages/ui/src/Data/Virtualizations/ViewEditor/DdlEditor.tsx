import { Alert, AlertActionCloseButton, Button, Card, CardBody, CardFooter } from '@patternfly/react-core';
import * as React from 'react';
import { Loader, PageSection } from '../../../Layout';
import { ITextEditor, TextEditor } from '../../../Shared';
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
   * The localized text for the save button.
   */
  i18nSaveLabel: string;

  /**
   * The localized text for the validate button.
   */
  i18nValidateLabel: string;

  /**
   * The localized text for the validate results alert title
   */
  i18nValidationResultsTitle: string;

  /**
   * `true` if all form fields have valid values.
   */
  isValid: boolean;

  /**
   * `true` if save is in progress.
   */
  isSaving: boolean;

  /**
   * `true` if validation is in progress.
   */
  isValidating: boolean;

  /**
   * View validationResults
   */
  validationResults: IViewEditValidationResult[];

  /**
   * Source table-columns for code completion
   */
  sourceTableInfos: ITableInfo[];

  /**
   * The callback for when the save button is clicked
   * @param ddl the text area ddl
   */
  onSave: (ddl: string) => void;

  /**
   * The callback for when the validate button is clicked.
   * @param ddl the ddl
   */
  onValidate: (ddl: string) => void;
}

export const DdlEditor: React.FunctionComponent<
IDdlEditorProps
> = props => {

  const [ddlValue, setDdlValue] = React.useState(props.viewDdl);
  const [initialDdlValue] = React.useState(props.viewDdl);
  const [needsValidation, setNeedsValidation] = React.useState(false);
  const [validationAlertVisible, setValidationAlertVisible] = React.useState(false);

  const handleDdlValidation = () => {
    props.onValidate(ddlValue);
    setNeedsValidation(false);
    setValidationAlertVisible(true);
  };

  const handleDdlChange = (editor: ITextEditor, data: any, value: string) => {
    setDdlValue(value);
    setNeedsValidation(true);
  }

  const handleSave = () => {
    props.onSave(ddlValue);
  };

  const hideValidationAlert = () => {
    setValidationAlertVisible(false);
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
    const result = {tables: {}};

    for (const tableInfo of tableInfos) {
      result.tables[tableInfo.name] = tableInfo.columnNames;
    }
    return result;
  }

  const editorOptions = {
    autofocus: true,
    extraKeys: { 'Ctrl-Space': 'autocomplete' },
    gutters: ['CodeMirror-lint-markers'],
    hintOptions: getHintOptions(props.sourceTableInfos),
    lineNumbers: true,
    lineWrapping: true,
    matchBrackets: true,
    mode: 'text/x-mysql',
    readOnly: false,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
    tabSize: 2,
  };

  return (
    <PageSection>
      <Card>
        <CardBody>
          {validationAlertVisible ? props.validationResults.map((e, idx) => (
            <Alert key={idx}
              variant={e.type}
              title={props.i18nValidationResultsTitle}
              action={<AlertActionCloseButton onClose={hideValidationAlert} />}
            >{e.message}
            </Alert>
          )) : null}
          <TextEditor
            value={initialDdlValue}
            options={editorOptions}
            onChange={handleDdlChange}
          />
        </CardBody>
        <CardFooter>
          <Button
            variant="secondary"
            className="ddl-editor__button"
            isDisabled={props.isValidating || !needsValidation}
            onClick={handleDdlValidation}
          >
            {props.isValidating ? (
              <Loader size={'sm'} inline={true} />
            ) : null}
            {props.i18nValidateLabel}
          </Button>
          <Button
            variant="primary"
            className="ddl-editor__button"
            isDisabled={
              props.isSaving ||
              props.isValidating ||
              !props.isValid ||
              needsValidation
            }
            onClick={handleSave}
          >
            {props.isSaving ? <Loader size={'xs'} inline={true} /> : null}
            {props.i18nSaveLabel}
          </Button>
        </CardFooter>
      </Card>
    </PageSection>
  );

}
