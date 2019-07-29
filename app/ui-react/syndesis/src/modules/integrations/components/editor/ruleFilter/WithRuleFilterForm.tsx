import { AutoForm, IAutoFormActions } from '@syndesis/auto-form';
import { FilterOptions, Op, StepKind } from '@syndesis/models';
import { toFormDefinition, validateRequiredProperties } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import './WithRuleFilterForm.css';

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

export const WithRuleFilterForm: React.FunctionComponent<
  IWithRuleFilterFormProps
> = ({ onUpdatedIntegration, filterOptions, step, children }) => {
  const { t } = useTranslation('shared');

  const onSave = async (
    values: IRuleFilterConfig,
    actions: IAutoFormActions<IRuleFilterConfig>
  ): Promise<void> => {
    await onUpdatedIntegration({
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
      displayName: t('integrations:editor:ruleForm:predicateDescription'),
      enum: [
        {
          label: t('integrations:editor:ruleForm:predicateEnumAll'),
          value: 'AND',
        },

        {
          label: t('integrations:editor:ruleForm:predicateEnumAny'),
          value: 'OR',
        },
      ],
      type: 'select',
    },
    rules: {
      arrayDefinition: {
        op: {
          defaultValue: 'contains',
          description: t('integrations:editor:ruleForm:operatorDescription'),
          displayName: t('integrations:editor:ruleForm:operatorDisplay'),
          enum: filterOptions.ops,
          order: 1,
          required: true,
          type: 'text',
        },
        path: {
          dataList: filterOptions.paths,
          description: t('integrations:editor:ruleForm:pathDescription'),
          displayName: t('integrations:editor:ruleForm:pathDisplay'),
          order: 0,
          placeholder: t('integrations:editor:ruleForm:pathPlaceholder'),
          required: true,
          type: 'text',
        },
        value: {
          description: t('integrations:editor:ruleForm:keywordsDescription'),
          displayName: t('integrations:editor:ruleForm:keywordsDisplay'),
          order: 2,
          placeholder: t('integrations:editor:ruleForm:keywordsPlaceholder'),
          required: true,
          type: 'text',
        },
      },
      arrayDefinitionOptions: {
        arrayControlAttributes: {
          className: 'form-group with-rule-filter-form__action',
        },
        formGroupAttributes: {
          className: 'with-rule-filter-form__group',
        },
        i18nAddElementText: t('integrations:editor:ruleForm:addRule'),
        minElements: 1
      },
      required: true,
      type: 'array',
    },
    type: {
      defaultValue: 'rule',
      type: 'hidden',
    },
  };
  const configuredProperties = step.configuredProperties || {};
  const config = {} as IRuleFilterConfig;
  config.rules =
    typeof configuredProperties.rules === 'string'
      ? JSON.parse(configuredProperties.rules)
      : configuredProperties.rules;
  config.predicate = configuredProperties.predicate || 'AND';
  config.type = configuredProperties.type || 'rule';
  const initialValue = config as IRuleFilterConfig;
  const validator = (values: IRuleFilterConfig) =>
    validateRequiredProperties(
      definition,
      (field: string) => '', // return an empty string here so the help text isn't replaced
      values
    );
  return (
    <div className="with-rule-filter-form">
      <AutoForm<IRuleFilterConfig>
        definition={toFormDefinition(definition)}
        i18nRequiredProperty={t('shared:requiredFieldMessage')}
        initialValue={initialValue}
        onSave={onSave}
        validate={validator}
        validateInitial={validator}
        key={step.id}
      >
        {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) =>
          children({
            form: <>{fields}</>,
            isSubmitting,
            isValid,
            submitForm,
          })
        }
      </AutoForm>
    </div>
  );
};
