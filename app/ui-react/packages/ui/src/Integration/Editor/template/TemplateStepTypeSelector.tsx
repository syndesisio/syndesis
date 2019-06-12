import { Text, TextContent } from '@patternfly/react-core';
import * as React from 'react';
import {
  LintFreemarker,
  LintMustache,
  LintVelocity,
} from '../../../Shared/TextEditor';

export type TemplateType = LintFreemarker | LintMustache | LintVelocity;

export interface ITemplateStepTypeSelectorProps {
  i18nSpecifyTemplateType: string;
  i18nFreemarkerLabel: string;
  i18nMustacheLabel: string;
  i18nVelocityLabel: string;
  templateType: TemplateType;
  onTemplateTypeChange: (type: TemplateType) => void;
}

export class TemplateStepTypeSelector extends React.Component<
  ITemplateStepTypeSelectorProps
> {
  constructor(props: ITemplateStepTypeSelectorProps) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { value } = event.currentTarget;
    this.props.onTemplateTypeChange(value as TemplateType);
  }
  public render() {
    return (
      <>
        <TextContent>
          <Text>{this.props.i18nSpecifyTemplateType}</Text>
        </TextContent>
        <div className="template-language-choices">
          <div className="radio-inline">
            <label htmlFor="freemarker-choice">
              <input
                type="radio"
                id="freemarker-choice"
                name="template-lang-choice"
                value={'freemarker'}
                checked={this.props.templateType === 'freemarker'}
                onChange={this.handleChange}
              />
              {this.props.i18nFreemarkerLabel}
            </label>
          </div>
          <div className="radio-inline">
            <label htmlFor="mustache-choice">
              <input
                type="radio"
                id="mustache-choice"
                name="template-lang-choice"
                value={'mustache'}
                checked={this.props.templateType === 'mustache'}
                onChange={this.handleChange}
              />
              {this.props.i18nMustacheLabel}
            </label>
          </div>
          <div className="radio-inline">
            <label htmlFor="velocity-choice">
              <input
                type="radio"
                id="velocity-choice"
                name="template-lang-choice"
                value={'velocity'}
                checked={this.props.templateType === 'velocity'}
                onChange={this.handleChange}
              />
              {this.props.i18nVelocityLabel}
            </label>
          </div>
        </div>
      </>
    );
  }
}
