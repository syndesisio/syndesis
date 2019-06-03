import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { IAutoFormActions } from '@syndesis/auto-form/src';
import { validateRequiredProperties } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
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

export const WithChoiceConfigurationForm: React.FunctionComponent<
  IWithChoiceConfigurationFormProps
> = ({ onUpdatedIntegration, stepId, initialValue, children }) => {
  const { t } = useTranslation(['integrations', 'shared']);

  const definition = {
    defaultFlowId: {
      type: 'hidden',
    },
    flowConditions: {
      arrayDefinition: {
        condition: {
          defaultValue: '',
          description: t('integrations:editor:choiceForm:conditionDescription'),
          displayName: t('integrations:editor:choiceForm:conditionName'),
          placeholder: t('integrations:editor:choiceForm:conditionPlaceholder'),
          required: true,
          type: 'text',
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
        i18nAddElementText: t('integrations:editor:choiceForm:addCondition'),
        minElements: 1,
        rowTitle: t('integrations:editor:choiceForm:addConditionTitle'),
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
      description: t(
        'integrations:editor:choiceForm:useDefaultFlowDescription'
      ),
      displayName: t('integrations:editor:choiceForm:useDefaultFlowTitle'),
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
      {({ fields, isSubmitting, isValid, submitForm }) =>
        children({
          fields,
          isSubmitting,
          isValid,
          submitForm,
        })
      }
    </AutoForm>
  );
};
