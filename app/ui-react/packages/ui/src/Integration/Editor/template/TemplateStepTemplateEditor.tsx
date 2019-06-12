import { Text, TextContent } from '@patternfly/react-core';

import * as React from 'react';
import {
  FreemarkerModeLint,
  ITextEditor,
  ITextEditorProps,
  MustacheModeLint,
  TextEditor,
  VelocityLint,
} from '../../../Shared';
import { TemplateType } from './TemplateStepTypeSelector';

export const TemplateStepLinters = {
  freemarker: new FreemarkerModeLint(),
  mustache: new MustacheModeLint(),
  velocity: new VelocityLint(),
};

interface ITemplateStepTemplateEditorProps extends ITextEditorProps {
  mode: TemplateType;
  textEditorDescription: React.ReactNode;
  initialValue: string;
  i18nFileUploadLimit: string;
  onUpdateLinting?: (
    unsortedAnnotations: any[],
    annotations: any[],
    editor: ITextEditor
  ) => void;
}

export const TemplateStepTemplateEditor: React.FunctionComponent<
  ITemplateStepTemplateEditorProps
> = ({
  textEditorDescription,
  i18nFileUploadLimit,
  initialValue,
  mode,
  onUpdateLinting,
  options = {},
  ...props
}) => {
  options = {
    dragDrop: true,
    gutters: ['CodeMirror-lint-markers'],
    lineNumbers: true,
    lineWrapping: true,
    lint: {
      lintOnChange: false,
      onUpdateLinting,
      tooltips: 'gutter',
    },
    mode,
    readOnly: false,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
    tabSize: 2,
    ...options,
  } as any;
  return (
    <>
      <TextContent>
        <Text>{textEditorDescription}</Text>
      </TextContent>
      <TextContent>
        <Text>
          <small>
            <i>{i18nFileUploadLimit}</i>
          </small>
        </Text>
      </TextContent>
      <TextEditor value={initialValue} options={options} {...props} />
    </>
  );
};
