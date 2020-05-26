import { Radio, Text, TextContent } from '@patternfly/react-core';
import * as React from 'react';
import { LintFreemarker, LintMustache, LintVelocity } from '../../../Shared/TextEditor';

export type TemplateType = LintFreemarker | LintMustache | LintVelocity;

export interface ITemplateStepTypeSelectorProps {
  i18nSpecifyTemplateType: string;
  i18nFreemarkerLabel: string;
  i18nMustacheLabel: string;
  i18nVelocityLabel: string;
  templateType: TemplateType;
  onTemplateTypeChange: (type: TemplateType) => void;
}

export const TemplateStepTypeSelector: React.FunctionComponent<ITemplateStepTypeSelectorProps> = ({
    i18nSpecifyTemplateType,
    i18nFreemarkerLabel,
    i18nMustacheLabel,
    i18nVelocityLabel,
    templateType,
    onTemplateTypeChange,
  }) => {
  const handleChange = (event: React.FormEvent<HTMLInputElement>) => {
    const { value } = event.currentTarget;
    onTemplateTypeChange(value as TemplateType);
  };
  return (
    <>
      <TextContent>
        <Text>{i18nSpecifyTemplateType}</Text>
      </TextContent>
      <div className="template-language-choices">
        <Radio
          id="freemarker-choice"
          name="template-lang-choice"
          value={'freemarker'}
          label={i18nFreemarkerLabel}
          isChecked={templateType === 'freemarker'}
          onChange={(_, evt) => handleChange(evt)}
        />
        <Radio
          id="mustache-choice"
          name="template-lang-choice"
          value={'mustache'}
          label={i18nMustacheLabel}
          checked={templateType === 'mustache'}
          onChange={(_, evt) => handleChange(evt)}
        />
        <Radio
          id="velocity-choice"
          name="template-lang-choice"
          value={'velocity'}
          label={i18nVelocityLabel}
          checked={templateType === 'velocity'}
          onChange={(_, evt) => handleChange(evt)}
        />
      </div>
    </>
  );
};
