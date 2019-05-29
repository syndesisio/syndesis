import * as CodeMirror from 'codemirror';
import * as React from 'react';
import { UnControlled as ReactCodeMirror } from 'react-codemirror2';
import { toValidHtmlId } from '../helpers';

import 'codemirror/addon/display/placeholder.js';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/lint/lint.js';
import 'codemirror/addon/mode/overlay.js';
import 'codemirror/lib/codemirror.css';
import 'codemirror/mode/velocity/velocity.js';

export { CodeMirror };
export type ITextEditor = CodeMirror.Editor;

export interface ITextEditorProps {
  id?: string;
  value: string;
  options: { [name: string]: any };
  onChange: (editor: ITextEditor, data: any, value: string) => void;
  editorDidMount?: (editor: ITextEditor) => void;
}

export class TextEditor extends React.Component<ITextEditorProps> {
  public render() {
    // Set default options here
    const options = { ...this.props.options };
    return (
      <>
        <div
          data-testid={`text-editor-${
            this.props.id ? toValidHtmlId(this.props.id) : 'codemirror'
          }`}
        >
          <ReactCodeMirror
            value={this.props.value}
            options={options}
            onChange={this.props.onChange}
            editorDidMount={this.props.editorDidMount}
          />
        </div>
      </>
    );
  }
}
