import { StringMap } from '@syndesis/models';
import {
  TemplateStepTemplateEditor,
  TemplateStepTypeSelector,
  TemplateType,
  TextEditor,
} from '@syndesis/ui';
import * as React from 'react';

export interface IWithTemplaterChildrenProps {
  controls: JSX.Element;
  submitForm(): any;
}
export interface IWithTemplaterProps {
  initialLanguage: TemplateType;
  initialText: string;
  onUpdatedIntegration(props: StringMap<any>): Promise<void>;
  children(props: IWithTemplaterChildrenProps): any;
}

export interface IWithTemplaterState {
  language: TemplateType;
  text: string;
}

export class WithTemplater extends React.Component<
  IWithTemplaterProps,
  IWithTemplaterState
> {
  constructor(props: IWithTemplaterProps) {
    super(props);
    this.state = {
      language: this.props.initialLanguage,
      text: this.props.initialText,
    };
    this.handleTemplateTypeChange = this.handleTemplateTypeChange.bind(this);
    this.handleEditorChange = this.handleEditorChange.bind(this);
  }
  public handleTemplateTypeChange(newType: TemplateType) {
    this.setState({ language: newType });
  }
  public handleEditorChange(editor: TextEditor, data: any, text: string) {
    this.setState({ text });
  }
  public render() {
    const submitForm = () => {
      this.props.onUpdatedIntegration({
        values: {
          language: this.state.language,
          template: this.state.text,
        },
      });
    };
    const controls = (
      <>
        <TemplateStepTypeSelector
          i18nSpecifyTemplateType={'Specify template type:'}
          i18nFreemarkerLabel={'Freemarker'}
          i18nMustacheLabel={'Mustache'}
          i18nVelocityLabel={'Velocity'}
          templateType={this.state.language as TemplateType}
          onTemplateTypeChange={this.handleTemplateTypeChange}
        />
        <TemplateStepTemplateEditor
          mode={this.state.language}
          i18nFileUploadLimit={'Max: 1 file (up to 1MB)'}
          textEditorDescription={
            <>
              Drag and drop a file, paste in text, or start typing in the text
              editor below to add a template. If you already have a template
              file, browse to upload the file.
            </>
          }
          initialValue={this.state.text}
          onChange={this.handleEditorChange}
        />
      </>
    );

    return this.props.children({
      controls,
      submitForm,
    });
  }
}
