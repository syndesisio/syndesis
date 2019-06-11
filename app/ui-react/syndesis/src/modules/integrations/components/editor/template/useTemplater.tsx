import { DataShapeKinds } from '@syndesis/api';
import { ActionDescriptor, StringMap } from '@syndesis/models';
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

export interface IUseTemplaterProps {
  initialLanguage: TemplateType;
  initialText: string;
  onUploadBrowse: () => void;
  onUpdatedIntegration(props: StringMap<any>): Promise<void>;
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

export function useTemplater(props: IUseTemplaterProps) {
  const isValid = React.useRef(false);
  const [editor, setEditor] = React.useState<ITextEditor>();
  const text = React.useRef(props.initialText || '');
  const language = React.useRef(props.initialLanguage);
  const linter = React.useRef(linters[props.initialLanguage]);
  React.useEffect(() => {
    if (props.initialText !== text.current) {
      text.current = props.initialText;
      doLint();
    }
    if (props.initialLanguage !== language.current) {
      language.current = props.initialLanguage;
      linter.current = linters[language.current];
    }
  }, [text, language, linter, props]);

  const handleEditorDidMount = (e: ITextEditor) => {
    setEditor(e);
    doLint();
  };

  const handleTemplateTypeChange = (newType: TemplateType) => {
    language.current = newType;
    linter.current = linters[language.current];
    if (typeof editor !== 'undefined') {
      editor.setOption('mode', linter.current.name());
      doLint();
    }
  };

  const handleEditorChange = (e: ITextEditor, data: any, t: string) => {
    text.current = t;
    doLint();
  };

  const handleUpdateLinting = (
    unsortedAnnotations: any[],
    annotations: any[]
  ) => {
    isValid.current = annotations.length === 0;
  };

  const submitForm = () => {
    props.onUpdatedIntegration({
      action: buildAction(),
      values: {
        language: language.current,
        template: text.current,
      },
    });
  };
  const templater = (
    <>
      <TemplateStepTypeSelector
        i18nSpecifyTemplateType={'Specify template type:'}
        i18nFreemarkerLabel={'Freemarker'}
        i18nMustacheLabel={'Mustache'}
        i18nVelocityLabel={'Velocity'}
        templateType={language.current}
        onTemplateTypeChange={handleTemplateTypeChange}
      />
      <TemplateStepTemplateEditor
        mode={language.current}
        i18nFileUploadLimit={'Max: 1 file (up to 1MB)'}
        textEditorDescription={
          <>
            Drag and drop a file, paste in text, or start typing in the text
            editor below to add a template. If you already have a template file,
            {/* eslint-disable-next-line */ ' '}
            <a
              data-testid={'with-templater-show-error-link'}
              onClick={props.onUploadBrowse}
            >
              browse to upload
            </a>{' '}
            the file.
          </>
        }
        initialValue={props.initialText}
        onChange={handleEditorChange}
        onUpdateLinting={handleUpdateLinting}
        editorDidMount={handleEditorDidMount}
      />
    </>
  );

  const doLint = () => {
    if (editor) {
      (editor as any).performLint();
    }
  };

  const buildAction = () => {
    try {
      const symbols = extractTemplateSymbols(
        text.current,
        linter.current.parse
      );
      const inputShapeSpecification = createSpecification(symbols);
      return {
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
    return {};
  };

  return {
    isValid: isValid.current,
    submitForm,
    templater,
  };
}
