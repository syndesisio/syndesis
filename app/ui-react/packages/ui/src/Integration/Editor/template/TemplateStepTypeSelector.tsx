import { Text, TextContent } from '@patternfly/react-core';
import * as React from 'react';

export enum TemplateType {
  Freemarker = 'freemarker',
  Mustache = 'mustache',
  Velocity = 'velocity',
}

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
            <input
              type="radio"
              id="freemarker-choice"
              name="template-lang-choice"
              value={TemplateType.Freemarker}
              checked={this.props.templateType === TemplateType.Freemarker}
              onChange={this.handleChange}
            />
            <label htmlFor="freemarker-choice">
              {this.props.i18nFreemarkerLabel}
            </label>
          </div>
          <div className="radio-inline">
            <input
              type="radio"
              id="mustache-choice"
              name="template-lang-choice"
              value={TemplateType.Mustache}
              checked={this.props.templateType === TemplateType.Mustache}
              onChange={this.handleChange}
            />
            <label htmlFor="mustache-choice">
              {this.props.i18nMustacheLabel}
            </label>
          </div>
          <div className="radio-inline">
            <input
              type="radio"
              id="velocity-choice"
              name="template-lang-choice"
              value={TemplateType.Velocity}
              checked={this.props.templateType === TemplateType.Velocity}
              onChange={this.handleChange}
            />
            <label htmlFor="velocity-choice">
              {this.props.i18nVelocityLabel}
            </label>
          </div>
        </div>
      </>
    );
  }
}
