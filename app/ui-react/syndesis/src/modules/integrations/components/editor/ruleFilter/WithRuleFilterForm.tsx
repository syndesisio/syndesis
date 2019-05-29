import { AutoForm, IAutoFormActions } from '@syndesis/auto-form';
import { FilterOptions, Op, StepKind } from '@syndesis/models';
import {
  toFormDefinition,
  validateConfiguredProperties,
  validateRequiredProperties,
} from '@syndesis/utils';
import * as React from 'react';

export interface IWithRuleFilterFormChildrenProps {
  form: JSX.Element;
  isValid: boolean;
  isSubmitting: boolean;
  submitForm(): any;
}

export interface IOnUpdatedIntegrationProps {
  /**
   * the configured values.
   */
  values: IRuleFilterConfigStringRules;
}

export interface IWithRuleFilterFormProps {
  filterOptions: FilterOptions;
  step: StepKind;
  children(props: IWithRuleFilterFormChildrenProps): any;
  onUpdatedIntegration(props: IOnUpdatedIntegrationProps): Promise<void>;
}

export interface IRuleFilterConfigStringRules {
  type: string;
  predicate: string;
  rules: string;
}

interface IRuleFilterConfig {
  type: string;
  predicate: string;
  rules: Op[];
}

export class WithRuleFilterForm extends React.Component<
  IWithRuleFilterFormProps
> {
  public render() {
    const onSave = async (
      values: IRuleFilterConfig,
      actions: IAutoFormActions<IRuleFilterConfig>
    ): Promise<void> => {
      await this.props.onUpdatedIntegration({
        values: {
          ...values,
          rules: JSON.stringify(values.rules || []),
        },
      });
      actions.setSubmitting(false);
    };
    const definition = {
      predicate: {
        defaultValue: 'AND',
        displayName: 'Continue only if incoming data match ',
        enum: [
          {
            label: 'ALL of the following',
            value: 'AND',
          },

          {
            label: 'ANY of the following',
            value: 'OR',
          },
        ],
        type: 'select',
      },
      rules: {
        arrayDefinition: {
          op: {
            description: 'Must meet this condition',
            displayName: 'Operator',
            enum: this.props.filterOptions.ops,
            order: 1,
            required: true,
            type: 'text',
          },
          path: {
            dataList: this.props.filterOptions.paths,
            description: 'The data you want to evaluate',
            displayName: 'Property Name',
            order: 0,
            placeholder: 'Property name',
            required: true,
            type: 'text',
          },
          value: {
            description: 'For this value',
            displayName: 'Keywords',
            order: 2,
            placeholder: 'Keywords',
            required: true,
            type: 'text',
          },
        },
        arrayDefinitionOptions: {
          arrayControlAttributes: {
            className: 'col-md-3 form-group',
          },
          controlLabelAttributes: {
            style: { display: 'none' },
          },
          formGroupAttributes: {
            className: 'col-md-3',
          },
          i18nAddElementText: '+ Add another rule',
          minElements: 1,
        },
        required: true,
        type: 'array',
      },
      type: {
        defaultValue: 'rule',
        type: 'hidden',
      },
    };
    const configuredProperties = this.props.step.configuredProperties || {};
    const config = {} as IRuleFilterConfig;
    config.rules =
      typeof configuredProperties.rules === 'string'
        ? JSON.parse(configuredProperties.rules)
        : configuredProperties.rules;
    config.predicate = configuredProperties.predicate || 'AND';
    config.type = configuredProperties.type || 'rule';
    const initialValue = config as IRuleFilterConfig;
    const isInitialValid = validateConfiguredProperties(
      definition,
      initialValue
    );
    return (
      <AutoForm<IRuleFilterConfig>
        definition={toFormDefinition(definition)}
        i18nRequiredProperty={'* Required field'}
        initialValue={initialValue}
        isInitialValid={isInitialValid}
        onSave={onSave}
        validate={(values: IRuleFilterConfig) => {
          return validateRequiredProperties(
            definition,
            (name: string) => `${name} is required`,
            values
          );
        }}
        key={this.props.step.id}
      >
        {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) =>
          this.props.children({
            form: <>{fields}</>,
            isSubmitting,
            isValid,
            submitForm,
          })
        }
      </AutoForm>
    );
  }
}
