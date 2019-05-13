import { DataShapeKinds } from '@syndesis/api';
import { Action, ActionDescriptor, StringMap } from '@syndesis/models';
import {
  ITextEditor,
  TemplateStepTemplateEditor,
  TemplateStepTypeSelector,
  TemplateType,
} from '@syndesis/ui';
import { key } from '@syndesis/utils';
import * as React from 'react';
import {
  FreemarkerModeLint,
  MustacheModeLint,
  TemplateSymbol,
  VelocityLint,
} from './codemirror';
import { AbstractLanguageLint } from './codemirror/abstract-language-lint';

export interface IWithTemplaterChildrenProps {
  controls: JSX.Element;
  submitForm(): any;
}
export interface IWithTemplaterProps {
  initialLanguage: TemplateType;
  initialText: string;
  onUpdateLinting: (
    unsortedAnnotations: any[],
    annotations: any[],
    editor: ITextEditor
  ) => void;
  onUpdatedIntegration(props: StringMap<any>): Promise<void>;
  children(props: IWithTemplaterChildrenProps): any;
}

export interface IWithTemplaterState {
  language: TemplateType;
  text: string;
}

const linters = {
  [TemplateType.Freemarker]: new FreemarkerModeLint(),
  [TemplateType.Mustache]: new MustacheModeLint(),
  [TemplateType.Velocity]: new VelocityLint(),
};

const outputShapeSpecification = createSpecification([
  new TemplateSymbol('message', 'string'),
]);

function extractTemplateSymbols(
  templateContent: string,
  parseFunction: (content: string) => TemplateSymbol[]
): TemplateSymbol[] {
  let symbols: TemplateSymbol[] = [];
  if (!templateContent) {
    return symbols;
  }
  symbols = parseFunction(templateContent);
  return symbols;
}

function createSpecification(symbols: TemplateSymbol[]): string {
  const spec: any = {
    $schema: 'http://json-schema.org/schema#',
    title: 'Template JSON Schema',
    type: 'object',
  };
  if (symbols.length === 0) {
    return spec;
  }
  const properties: any = {};
  for (const symbol of symbols) {
    properties[symbol.getId()] = {
      description: 'Identifier for the symbol ' + symbol.getId(),
      type: symbol.getType(),
    };
  }
  spec.properties = properties;
  return JSON.stringify(spec);
}

export class WithTemplater extends React.Component<
  IWithTemplaterProps,
  IWithTemplaterState
> {
  private linter: AbstractLanguageLint;
  private editor: ITextEditor | undefined;
  private action: Action | undefined;
  constructor(props: IWithTemplaterProps) {
    super(props);
    this.state = {
      language: this.props.initialLanguage,
      text: this.props.initialText,
    };
    this.linter = linters[this.props.initialLanguage];
    this.handleTemplateTypeChange = this.handleTemplateTypeChange.bind(this);
    this.handleEditorChange = this.handleEditorChange.bind(this);
    this.handleEditorDidMount = this.handleEditorDidMount.bind(this);
  }
  public handleEditorDidMount(editor: ITextEditor) {
    this.editor = editor;
    this.doLint();
    this.buildAction(this.state.text);
  }
  public handleTemplateTypeChange(newType: TemplateType) {
    this.linter = linters[newType];
    if (typeof this.editor !== 'undefined') {
      this.editor.setOption('mode', this.linter.name());
      this.doLint();
    }
    this.buildAction(this.state.text);
    this.setState({ language: newType });
  }
  public handleEditorChange(editor: ITextEditor, data: any, text: string) {
    this.buildAction(this.state.text);
    this.setState({ text });
  }
  public render() {
    const submitForm = () => {
      this.props.onUpdatedIntegration({
        action: this.action,
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
          onUpdateLinting={this.props.onUpdateLinting}
          editorDidMount={this.handleEditorDidMount}
        />
      </>
    );
    return this.props.children({
      controls,
      submitForm,
    });
  }
  private doLint() {
    if (this.editor) {
      (this.editor as any).performLint();
    }
  }
  private buildAction(text: string) {
    try {
      const symbols = extractTemplateSymbols(text, this.linter.parse);
      const inputShapeSpecification = createSpecification(symbols);
      this.action = {
        actionType: 'step',
        descriptor: {
          inputDataShape: {
            kind: DataShapeKinds.JSON_SCHEMA,
            name: 'Template JSON Schema',
            specification: inputShapeSpecification,
          } as any /* todo: type hack */,
          outputDataShape: {
            kind: DataShapeKinds.JSON_SCHEMA,
            name: 'Template JSON Schema',
            specification: outputShapeSpecification,
          } as any /* todo: type hack */,
        } as ActionDescriptor,
        id: key(),
        name: 'Templater',
      };
    } catch (err) {
      // ignore
    }
  }
}
