import {
  applyUserDefinedDataShapesToAction,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
} from '@syndesis/api';
import { AutoForm, IFormValue } from '@syndesis/auto-form';
import {
  Action,
  ActionDescriptor,
  IConfigurationProperties,
} from '@syndesis/models';
import { IntegrationEditorForm } from '@syndesis/ui';
import {
  allFieldsRequired,
  coerceFormValues,
  getRequiredStatusText,
  toFormDefinition,
  validateRequiredProperties,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../../../i18n';
import { IWithConfigurationFormProps } from './WithConfigurationForm';

export interface IConfigurationFormProps
  extends Pick<IWithConfigurationFormProps, 'configurationPage'>,
    Pick<IWithConfigurationFormProps, 'initialValue'>,
    Pick<IWithConfigurationFormProps, 'oldAction'>,
    Pick<IWithConfigurationFormProps, 'onUpdatedIntegration'>,
    Pick<IWithConfigurationFormProps, 'chooseActionHref'> {
  action: Action;
  descriptor: ActionDescriptor;
  definitionOverride?: IConfigurationProperties;
  children: any;
}

export const ConfigurationForm: React.FunctionComponent<
  IConfigurationFormProps
> = ({
  action,
  configurationPage,
  descriptor,
  definitionOverride,
  initialValue,
  oldAction,
  chooseActionHref,
  onUpdatedIntegration,
  children,
}) => {
  const { t } = useTranslation('shared');
  const [error, setError] = React.useState();
  try {
    const steps = getActionSteps(descriptor);
    const step = getActionStep(steps, configurationPage);
    const definition = definitionOverride || getActionStepDefinition(step);
    const moreConfigurationSteps = configurationPage < steps.length - 1;
    const onSave = async (
      values: { [key: string]: string },
      actions: any
    ): Promise<void> => {
      try {
        action =
          typeof oldAction !== 'undefined'
            ? applyUserDefinedDataShapesToAction(oldAction, {
                ...action,
                descriptor,
              })
            : action;
        await onUpdatedIntegration({
          action,
          moreConfigurationSteps,
          values: coerceFormValues(values),
        });
      } catch (e) {
        setError(e.message);
      }
      actions.setSubmitting(false);
    };
    const key = JSON.stringify(definition);
    const requiredPrompt = getRequiredStatusText(
      definition,
      i18n.t('shared:AllFieldsRequired'),
      i18n.t('shared:FieldsMarkedWithStarRequired'),
      ''
    );
    const validator = (values: IFormValue) =>
      validateRequiredProperties(
        definition,
        (name: string) => `${name} is required`,
        values
      );
    const formTitle =
      typeof step.description === 'undefined'
        ? action.name
        : `${action.name} - ${step.description}`;
    return (
      <AutoForm<IFormValue>
        i18nRequiredProperty={t('shared:requiredFieldMessage')}
        allFieldsRequired={allFieldsRequired(definition)}
        i18nFieldsStatusText={requiredPrompt}
        definition={toFormDefinition(definition)}
        initialValue={initialValue as IFormValue}
        onSave={onSave}
        validate={validator}
        validateInitial={validator}
        key={key}
      >
        {({ fields, handleSubmit, isValid, isSubmitting, submitForm }) => (
          <>
            <IntegrationEditorForm
              i18nFormTitle={formTitle}
              i18nBackAction={'Choose Action'}
              i18nNext={'Next'}
              isValid={isValid}
              isLoading={isSubmitting}
              submitForm={() => {
                setError(undefined);
                submitForm();
              }}
              handleSubmit={handleSubmit}
              backActionHref={chooseActionHref}
              error={error}
            >
              {fields}
            </IntegrationEditorForm>
          </>
        )}
      </AutoForm>
    );
  } catch (e) {
    return children;
  }
};
