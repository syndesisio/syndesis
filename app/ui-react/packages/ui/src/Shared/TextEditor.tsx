import * as React from 'react';
import { ICodeMirror, UnControlled as CodeMirror } from 'react-codemirror2';

import 'codemirror/lib/codemirror.css';

export type ITextEditor = ICodeMirror;

export interface ITextEditorProps {
  value: string;
  options: { [name: string]: any };
  onChange: (editor: ITextEditor, data: any, value: string) => void;
}

export class TextEditor extends React.Component<ITextEditorProps> {
  public render() {
    // Set default options here
    const options = { ...this.props.options };
    return (
      <>
        <CodeMirror
          value={this.props.value}
          options={options}
          onChange={this.props.onChange}
        />
      </>
    );
  }
}
