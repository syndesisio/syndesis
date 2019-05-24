import { Text, TextContent } from '@patternfly/react-core';
import { Alert } from 'patternfly-react';
import * as React from 'react';
import { ITextEditor, TextEditor } from '../../../Shared';
import { TemplateType } from './TemplateStepTypeSelector';

interface ITemplateStepTemplateEditorProps {
  mode: TemplateType;
  textEditorDescription: React.ReactNode;
  onChange: (editor: ITextEditor, data: any, value: string) => void;
  initialValue: string;
  i18nFileUploadLimit: string;
  invalidFileMessage?: string;
  editorDidMount?: (editor: ITextEditor) => void;
  onUpdateLinting?: (
    unsortedAnnotations: any[],
    annotations: any[],
    editor: ITextEditor
  ) => void;
}

export class TemplateStepTemplateEditor extends React.Component<
  ITemplateStepTemplateEditorProps
> {
  public render() {
    const editorOptions = {
      dragDrop: false,
      gutters: ['CodeMirror-lint-markers'],
      lineNumbers: true,
      lineWrapping: true,
      lint: {
        lintOnChange: false,
        onUpdateLinting: this.props.onUpdateLinting,
        tooltips: 'gutter',
      },
      mode: this.props.mode,
      readOnly: false,
      showCursorWhenSelecting: true,
      styleActiveLine: true,
      tabSize: 2,
    };
    return (
      <>
        <TextContent>
          <Text>{this.props.textEditorDescription}</Text>
        </TextContent>
        <TextContent>
          <Text>
            <small>
              <i>{this.props.i18nFileUploadLimit}</i>
            </small>
          </Text>
        </TextContent>
        {this.props.invalidFileMessage && this.props.invalidFileMessage !== '' && (
          <Alert type="warning">
            <span
              dangerouslySetInnerHTML={{
                __html: this.props.invalidFileMessage,
              }}
            />
          </Alert>
        )}
        <TextEditor
          onChange={this.props.onChange}
          options={editorOptions}
          value={this.props.initialValue}
          editorDidMount={this.props.editorDidMount}
        />
      </>
    );
  }
}
