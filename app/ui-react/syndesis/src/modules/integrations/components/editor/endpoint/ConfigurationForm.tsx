import {
  applyUserDefinedDataShapesToAction,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
} from '@syndesis/api';
import { AutoForm } from '@syndesis/auto-form';
import { Action, ActionDescriptor } from '@syndesis/models';
import { IntegrationEditorForm } from '@syndesis/ui';
import {
  allFieldsRequired,
  applyInitialValues,
  getRequiredStatusText,
  toFormDefinition,
  validateConfiguredProperties,
} from '@syndesis/utils';
import * as React from 'react';
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
    initialValue = applyInitialValues(definition, initialValue);
    const isInitialValid = validateConfiguredProperties(
      definition,
      initialValue
    );
    const requiredPrompt = getRequiredStatusText(
      definition,
      i18n.t('shared:AllFieldsRequired'),
      i18n.t('shared:FieldsMarkedWithStarRequired'),
      ''
    );
    return (
      <AutoForm<{ [key: string]: string }>
        i18nRequiredProperty={'* Required field'}
        allFieldsRequired={allFieldsRequired(definition)}
        i18nFieldsStatusText={requiredPrompt}
        definition={toFormDefinition(definition)}
        initialValue={initialValue}
        isInitialValid={isInitialValid}
        onSave={onSave}
        validate={(values: { [name: string]: any }): any =>
          validateConfiguredProperties(definition, values)
        }
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
