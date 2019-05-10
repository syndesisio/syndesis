import { Text, TextContent } from '@patternfly/react-core';
import * as React from 'react';
import { TextEditor } from '../../../../Shared';
import { TemplateType } from './TemplateStepTypeSelector';

interface ITemplateStepTemplateEditorProps {
  mode: TemplateType;
  textEditorDescription: React.ReactNode;
  onChange: (editor: any, data: any, value: string) => void;
  initialValue: string;
  i18nFileUploadLimit: string;
}

export class TemplateStepTemplateEditor extends React.Component<
  ITemplateStepTemplateEditorProps
> {
  public render() {
    const editorOptions = {
      gutters: ['CodeMirror-lint-markers'],
      lineNumbers: false,
      lineWrapping: true,
      lint: true,
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
        <TextEditor
          onChange={this.props.onChange}
          options={editorOptions}
          value={this.props.initialValue}
        />
      </>
    );
  }
}
