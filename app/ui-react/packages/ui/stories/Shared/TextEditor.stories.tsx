import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ITextEditor, TextEditor } from '../../src/Shared';

const stories = storiesOf('Shared/TextEditor', module);

stories.add('Normal', () => (
  <TextEditorStory initialValue={'\n\n\nHello world!\n\n\n'} />
));

interface ITextEditorStoryProps {
  initialValue: string;
}

interface ITextEditorStoryState {
  value: string;
}

class TextEditorStory extends React.Component<
  ITextEditorStoryProps,
  ITextEditorStoryState
> {
  constructor(props: ITextEditorStoryProps) {
    super(props);
    this.state = {
      value: this.props.initialValue,
    };
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(editor: ITextEditor, data: any, value: string) {
    this.setState({
      value,
    });
  }
  public render() {
    return (
      <TextEditor
        value={this.state.value}
        options={{}}
        onChange={this.handleChange}
      />
    );
  }
}
