import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { IAutoFormActions } from '@syndesis/auto-form/src';
import { FilterOptions } from "@syndesis/models";
import { validateRequiredProperties } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { IChoiceFormConfiguration } from './interfaces';

export interface IWithChoiceConfigurationFormChildrenProps {
  fields: JSX.Element;
  isValid: boolean;
  isSubmitting: boolean;
  values: IChoiceFormConfiguration;
  submitForm(): void;
}

export interface IWithChoiceConfigurationFormProps {
  configMode: 'basic' | 'advanced';
  filterOptions: FilterOptions;
  initialValue: IChoiceFormConfiguration;
  stepId: string;
  onUpdatedIntegration(props: IChoiceFormConfiguration): Promise<void>;
  children(props: IWithChoiceConfigurationFormChildrenProps): any;
}

export const WithChoiceConfigurationForm: React.FunctionComponent<
  IWithChoiceConfigurationFormProps
> = ({ onUpdatedIntegration, filterOptions, stepId, initialValue, children, configMode }) => {
  const { t } = useTranslation(['integrations', 'shared']);

  const basicMode = configMode === 'basic';
  const advancedMode = configMode === 'advanced';

  const definition = {
    defaultFlowId: {
      order: 6,
      type: 'hidden',
    },
    flowConditions: {
      arrayDefinition: {
        condition: {
          defaultValue: '',
          description: t('integrations:editor:choiceForm:conditionDescription'),
          displayName: '',
          order: 0,
          placeholder: t('integrations:editor:choiceForm:conditionPlaceholder'),
          required: advancedMode,
          type: advancedMode ? 'text' : 'hidden',
        },
        flowId: {
          defaultValue: '',
          formGroupAttributes: {
            style: {
              display: 'none',
            },
          },
          type: 'hidden',
        },
        op: {
          defaultValue: 'contains',
          description: t('integrations:editor:choiceForm:operatorDescription'),
          displayName: '',
          enum: filterOptions.ops,
          order: 2,
          required: basicMode,
          type: basicMode ? 'text' : 'hidden',
        },
        path: {
          dataList: filterOptions.paths,
          description: t('integrations:editor:choiceForm:pathDescription'),
          displayName: '',
          order: 1,
          placeholder: t('integrations:editor:choiceForm:pathPlaceholder'),
          required: basicMode,
          type: basicMode ? 'text' : 'hidden',
        },
        value: {
          description: t('integrations:editor:choiceForm:keywordsDescription'),
          displayName: '',
          order: 3,
          placeholder: t('integrations:editor:choiceForm:keywordsPlaceholder'),
          required: basicMode,
          type: basicMode ? 'text' : 'hidden',
        },
      },
      arrayDefinitionOptions: {
        arrayControlAttributes: {
          className: 'conditional-flow__controls',
        },
        arrayRowTitleAttributes: {
          className: 'conditional-flow__title',
        },
        controlLabelAttributes: {
          style: { display: 'none' },
        },
        formGroupAttributes: {
          className: 'conditional-flow__form-group',
        },
        i18nAddElementText: t('integrations:editor:choiceForm:addCondition'),
        minElements: 1,
        rowTitle: t('integrations:editor:choiceForm:addConditionTitle'),
        showSortControls: true,
      },
      order: 1,
      required: true,
      type: 'array',
    },
    forAllIncomingData: {
      displayName: t('integrations:editor:choiceForm:forAllIncomingData'),
      order: 0,
      type: 'legend',
    },
    otherwise: {
      displayName: t('integrations:editor:choiceForm:otherwise'),
      order: 2,
      type: 'legend'
    },
    routingScheme: {
      defaultValue: 'direct',
      order: 5,
      type: 'hidden',
    },
    useDefaultFlow: {
      defaultValue: 'false',
      displayName: t('integrations:editor:choiceForm:useDefaultFlowTitle'),
      order: 3,
      type: 'boolean',
    },
  } as IFormDefinition;

  const onSave = (
    values: IChoiceFormConfiguration,
    actions: IAutoFormActions<IChoiceFormConfiguration>
  ) => {
    onUpdatedIntegration(values);
    actions.setSubmitting(false);
  };

  const validator = (values: IChoiceFormConfiguration) => {
    return validateRequiredProperties(
      definition,
      (field: string) =>
        t('integrations:editor:choiceForm:fieldRequired', { field }),
      values
    );
  };

  return (
    <AutoForm<IChoiceFormConfiguration>
      key={stepId}
      definition={definition}
      i18nRequiredProperty={t('shared:requiredFieldMessage')}
      initialValue={initialValue}
      validate={validator}
      validateInitial={validator}
      onSave={onSave}
    >
      {({ fields, isSubmitting, isValid, submitForm, values }) =>
        children({
          fields,
          isSubmitting,
          isValid,
          submitForm,
          values,
        })
      }
    </AutoForm>
  );
};
