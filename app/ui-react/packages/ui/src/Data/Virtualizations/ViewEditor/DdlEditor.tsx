import { Alert, AlertActionCloseButton, Button, Card, CardBody, CardFooter } from '@patternfly/react-core';
import * as React from 'react';
import { Loader, PageSection } from '../../../Layout';
import { ITextEditor, TextEditor } from '../../../Shared';
import './DdlEditor.css';

export interface IViewEditValidationResult {
  message: string;
  type: 'danger' | 'success';
}

interface ITableInfo {
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

interface IDdlEditorState {
  ddlValue: string;
  initialDdlValue: string;
  needsValidation: boolean;
  validationAlertVisible: boolean;
}

export class DdlEditor extends React.Component<
  IDdlEditorProps,
  IDdlEditorState
> {
  public static defaultProps = {
    validationResults: [],
  };

  constructor(props: IDdlEditorProps) {
    super(props);
    this.state = {
      ddlValue: this.props.viewDdl,
      initialDdlValue: this.props.viewDdl,
      needsValidation: false,
      validationAlertVisible: false
    };
    this.handleDdlChange = this.handleDdlChange.bind(this);
    this.handleDdlValidation = this.handleDdlValidation.bind(this);
    this.handleSave = this.handleSave.bind(this);
    this.hideValidationAlert = this.hideValidationAlert.bind(this);
  }

  public handleDdlValidation = () => {
    this.props.onValidate(this.state.ddlValue);
    this.setState({
      needsValidation: false,
      validationAlertVisible: true
    });
  };

  public handleDdlChange(editor: ITextEditor, data: any, value: string) {
    this.setState({
      ddlValue: value,
      needsValidation: true,
    });
  }

  public handleSave = () => (event: any) => {
    const currentDdl = this.state.ddlValue;
    this.props.onSave(currentDdl);
  };

  public hideValidationAlert = () => {
    this.setState({
      validationAlertVisible: false,
    });
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
  public getHintOptions(tableInfos: ITableInfo[]) {
    const result = {tables: {}};

    for (const tableInfo of tableInfos) {
      result.tables[tableInfo.name] = tableInfo.columnNames;
    }
    return result;
  }

  public render() {
    const { validationAlertVisible } = this.state;

    const editorOptions = {
      autofocus: true,
      extraKeys: { 'Ctrl-Space': 'autocomplete' },
      gutters: ['CodeMirror-lint-markers'],
      hintOptions: this.getHintOptions(this.props.sourceTableInfos),
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
            {validationAlertVisible ? this.props.validationResults.map((e, idx) => (
              <Alert key={idx}
                variant={e.type}
                title="DDL Validation"
                action={<AlertActionCloseButton onClose={this.hideValidationAlert} />}
              >{e.message}
              </Alert>
            )) : null}
            <TextEditor
              value={this.state.initialDdlValue}
              options={editorOptions}
              onChange={this.handleDdlChange}
            />
          </CardBody>
          <CardFooter>
            <Button
              variant="secondary" 
              className="ddl-editor__button"
              isDisabled={this.props.isValidating || !this.state.needsValidation}
              onClick={this.handleDdlValidation}
            >
              {this.props.isValidating ? (
                <Loader size={'sm'} inline={true} />
              ) : null}
              {this.props.i18nValidateLabel}
            </Button>
            <Button
              variant="primary"
              className="ddl-editor__button"
              isDisabled={
                this.props.isSaving ||
                this.props.isValidating ||
                !this.props.isValid ||
                this.state.needsValidation
              }
              onClick={this.handleSave()}
            >
              {this.props.isSaving ? <Loader size={'xs'} inline={true} /> : null}
              {this.props.i18nSaveLabel}
            </Button>
          </CardFooter>
        </Card>
      </PageSection>
    );
  }
}
