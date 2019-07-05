import { DataShapeKinds } from '@syndesis/api';
import { ActionDescriptor, StringMap } from '@syndesis/models';
import {
  AbstractLanguageLint,
  ITextEditor,
  TemplateStepLinters,
  TemplateStepTemplateEditor,
  TemplateStepTypeSelector,
  TemplateSymbol,
  TemplateType,
} from '@syndesis/ui';
import { key } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';

export interface IUseTemplateProps {
  initialLanguage: TemplateType;
  initialText: string;
  onUploadBrowse: () => void;
  onUpdatedIntegration(props: StringMap<any>): Promise<void>;
}

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
    return JSON.stringify(spec);
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

export function useTemplate(props: IUseTemplateProps) {
  const { t } = useTranslation('integrations');
  const [isValid, setIsValid] = React.useState(false);
  const editor = React.useRef<ITextEditor>();
  const templateContent = React.useRef(props.initialText || '');
  const [language, setLanguage] = React.useState(props.initialLanguage);
  const linter = React.useRef<AbstractLanguageLint>(
    TemplateStepLinters[language]
  );

  const setText = (text: string) => {
    if (editor.current) {
      editor.current.setValue(text);
    }
  };

  const handleEditorDidMount = (e: ITextEditor) => {
    editor.current = e;
  };

  const handleTemplateTypeChange = (newType: TemplateType) => {
    setLanguage(newType);
    linter.current = TemplateStepLinters[newType];
  };

  const handleEditorChange = (e: ITextEditor, data: any, text: string) => {
    templateContent.current = text;
  };

  const handleUpdateLinting = (
    unsortedAnnotations: any[],
    annotations: any[]
  ) => {
    setIsValid(annotations.length === 0);
  };

  const submitForm = () => {
    props.onUpdatedIntegration({
      action: buildAction(),
      values: {
        language,
        template: templateContent.current,
      },
    });
  };

  const buildAction = () => {
    let spec = {};
    try {
      const symbols = extractTemplateSymbols(
        templateContent.current,
        linter.current.parse
      );
      const inputShapeSpecification = createSpecification(symbols);
      spec = {
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
        name: 'Template',
      };
    } catch (err) {
      // ignore
    }
    return spec;
  };

  const formatAnnotation = (a: any) => {
    a.message = t(`integrations:linter:${a.message}`, a.messageContext);
    return a;
  };

  const template = (
    <>
      <TemplateStepTypeSelector
        i18nSpecifyTemplateType={'Specify template type:'}
        i18nFreemarkerLabel={'Freemarker'}
        i18nMustacheLabel={'Mustache'}
        i18nVelocityLabel={'Velocity'}
        templateType={language}
        onTemplateTypeChange={handleTemplateTypeChange}
      />
      <TemplateStepTemplateEditor
        mode={language}
        i18nFileUploadLimit={'Max: 1 file (up to 1MB)'}
        textEditorDescription={
          <>
            Drag and drop a file, paste in text, or start typing in the text
            editor below to add a template. If you already have a template file,
            {/* eslint-disable-next-line */ ' '}
            <a
              data-testid={'with-template-show-error-link'}
              onClick={props.onUploadBrowse}
            >
              browse to upload
            </a>{' '}
            the file.
          </>
        }
        initialValue={props.initialText}
        onChange={handleEditorChange}
        formatAnnotation={formatAnnotation}
        onUpdateLinting={handleUpdateLinting}
        editorDidMount={handleEditorDidMount}
      />
    </>
  );
  return {
    isValid,
    setText,
    submitForm,
    template,
  };
}
