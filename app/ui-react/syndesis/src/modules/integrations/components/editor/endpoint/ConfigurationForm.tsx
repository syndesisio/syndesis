import {
  applyUserDefinedDataShapesToAction,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
} from '@syndesis/api';
import { AutoForm, IFormValue } from '@syndesis/auto-form';
import { Action, ActionDescriptor } from '@syndesis/models';
import { IntegrationEditorForm } from '@syndesis/ui';
import {
  allFieldsRequired,
  getRequiredStatusText,
  toFormDefinition,
  validateRequiredProperties,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../../../i18n';
import { IWithConfigurationFormProps } from './WithConfigurationForm';

export interface IConfigurationFormProps
  extends Pick<IWithConfigurationFormProps, 'configurationStep'>,
    Pick<IWithConfigurationFormProps, 'initialValue'>,
    Pick<IWithConfigurationFormProps, 'oldAction'>,
    Pick<IWithConfigurationFormProps, 'onUpdatedIntegration'>,
    Pick<IWithConfigurationFormProps, 'chooseActionHref'> {
  action: Action;
  descriptor: ActionDescriptor;
  children: any;
}

export const ConfigurationForm: React.FunctionComponent<
  IConfigurationFormProps
> = ({
  action,
  configurationStep,
  descriptor,
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
    const step = getActionStep(steps, configurationStep);
    const definition = getActionStepDefinition(step);
    const moreConfigurationSteps = configurationStep < steps.length - 1;
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
          values,
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
        {({ fields, handleSubmit, isValid, submitForm }) => (
          <>
            <IntegrationEditorForm
              i18nFormTitle={`${action.name} - ${action.description}`}
              i18nBackAction={'Choose Action'}
              i18nNext={'Next'}
              isValid={isValid}
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
