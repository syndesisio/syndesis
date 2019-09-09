import { 
  Alert, 
  AlertActionCloseButton, 
  Button, 
  Card, 
  CardBody, 
  CardFooter, 
  Title 
} from '@patternfly/react-core';
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
   */
  onSave: (ddl: string) => void;
}

export const DdlEditor: React.FunctionComponent<
IDdlEditorProps
> = props => {

  const [ddlValue, setDdlValue] = React.useState(props.viewDdl);
  const [initialDdlValue] = React.useState(props.viewDdl);

  const handleCloseValidationMessage = () => {
    props.onCloseValidationMessage();
  }

  const handleDdlChange = (editor: ITextEditor, data: any, value: string) => {
    setDdlValue(value);
    handleCloseValidationMessage();
  }

  const handleFinish = () => {
    props.onFinish();
  };

  const handleSave = () => {
    props.onSave(ddlValue);
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
    <PageSection variant={'light'}>
      <Title headingLevel="h5" size="lg">
        {props.i18nTitle}
      </Title>
      <Card>
        <CardBody>
          {props.showValidationMessage ? props.validationResults.map((e, idx) => (
            <Alert key={idx}
              variant={e.type}
              title={props.i18nValidationResultsTitle}
              action={<AlertActionCloseButton onClose={handleCloseValidationMessage} />}
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
            data-testid={'ddl-editor-done-button'}
            variant="secondary"
            className="ddl-editor__button"
            isDisabled={false}
            onClick={handleFinish}
          >
            {props.i18nDoneLabel}
          </Button>
          <Button
            data-testid={'ddl-editor-save-button'}
            variant="primary"
            className="ddl-editor__button"
            isDisabled={props.isSaving}
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
