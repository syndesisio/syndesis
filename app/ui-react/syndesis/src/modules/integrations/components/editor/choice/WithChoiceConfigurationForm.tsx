import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { IAutoFormActions } from '@syndesis/auto-form/src';
import { validateRequiredProperties } from '@syndesis/utils';
import * as React from 'react';
import { IChoiceFormConfiguration } from './interfaces';

export interface IWithChoiceConfigurationFormChildrenProps {
  fields: JSX.Element;
  isValid: boolean;
  isSubmitting: boolean;
  submitForm(): void;
}

export interface IWithChoiceConfigurationFormProps {
  initialValue: IChoiceFormConfiguration;
  stepId: string;
  onUpdatedIntegration(props: IChoiceFormConfiguration): Promise<void>;
  children(props: IWithChoiceConfigurationFormChildrenProps): any;
}

export class WithChoiceConfigurationForm extends React.Component<
  IWithChoiceConfigurationFormProps
> {
  public render() {
    const definition = {
      flowConditions: {
        arrayDefinition: {
          condition: {
            description: 'Provide a condition that you want to evaluate.',
            displayName: 'Condition',
            placeholder: 'Condition',
            required: true,
            type: 'text',
          },
          flowId: {
            formGroupAttributes: {
              style: {
                display: 'none',
              },
            },
            type: 'hidden',
          },
        },
        arrayDefinitionOptions: {
          arrayControlAttributes: {
            className: 'col-md-2 form-group',
          },
          arrayRowTitleAttributes: {
            className: 'col-md-2',
          },
          controlLabelAttributes: {
            style: { display: 'none' },
          },
          formGroupAttributes: {
            className: 'col-md-8',
          },
          i18nAddElementText: '+ Add another condition',
          minElements: 1,
          rowTitle: 'When',
          showSortControls: true,
        },
        required: true,
        type: 'array',
      },
      routingScheme: {
        defaultValue: 'direct',
        type: 'hidden',
      },
      useDefaultFlow: {
        defaultValue: 'false',
        description: 'Use this flow when no other condition matches',
        displayName: 'Use a default flow',
        type: 'boolean',
      },
    } as IFormDefinition;

    const onSave = (
      values: IChoiceFormConfiguration,
      actions: IAutoFormActions<IChoiceFormConfiguration>
    ) => {
      this.props.onUpdatedIntegration(values);
      actions.setSubmitting(false);
    };

    const validator = (values: IChoiceFormConfiguration) => {
      return validateRequiredProperties(
        definition,
        (name: string) => `${name} is required`,
        values
      );
    };

    return (
      <AutoForm<IChoiceFormConfiguration>
        key={this.props.stepId}
        definition={definition}
        i18nRequiredProperty={'* Required field'}
        initialValue={this.props.initialValue}
        validate={validator}
        validateInitial={validator}
        onSave={onSave}
      >
        {({ fields, isSubmitting, isValid, submitForm }) =>
          this.props.children({
            fields,
            isSubmitting,
            isValid,
            submitForm,
          })
        }
      </AutoForm>
    );
  }
}
