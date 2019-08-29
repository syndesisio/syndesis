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
      order: 6,
      type: 'hidden',
    },

    flowConditions: {
      arrayDefinition: {
        condition: {
          defaultValue: '',
          description: t('integrations:editor:choiceForm:conditionDescription'),
          displayName: '',
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
