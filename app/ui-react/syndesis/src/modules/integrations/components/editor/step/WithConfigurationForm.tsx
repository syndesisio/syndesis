import {
  ALL_STEPS,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
} from '@syndesis/api';
import { AutoForm, IFormValue } from '@syndesis/auto-form';
import { ConfigurationProperty, StepKind } from '@syndesis/models';
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

export interface IWithConfigurationFormChildrenProps {
  /**
   * the form (embedded in the right UI elements)
   */
  form: JSX.Element;
  /**
   * true if the form contains valid values. Can be used to enable/disable the
   * submit button.
   */
  isValid: boolean;
  /**
   * true if the form is being submitted. Can be used to enable/disable the
   * submit button.
   */
  isSubmitting: boolean;
  /**
   * the callback to fire to submit the form.
   */
  submitForm(): any;
}

export interface IOnUpdatedIntegrationProps {
  /**
   * the configured values.
   */
  values: { [key: string]: string } | null;
}

export interface IWithConfigurationFormProps {
  /**
   * the ID of the action that needs to be configured.
   */
  step: StepKind;
  /**
   * the render prop that will receive the ready-to-be-rendered form and some
   * helpers.
   *
   * @see [form]{@link IWithConfigurationFormChildrenProps#form}
   * @see [isValid]{@link IWithConfigurationFormChildrenProps#isValid}
   * @see [isSubmitting]{@link IWithConfigurationFormChildrenProps#isSubmitting}
   * @see [onSubmit]{@link IWithConfigurationFormChildrenProps#submitForm}
   */
  children(props: IWithConfigurationFormChildrenProps): any;

  /**
   * the callback that is fired after the form submit with valid values.
   *
   * @see [action]{@link IOnUpdatedIntegrationProps#action}
   * @see [moreConfigurationSteps]{@link IOnUpdatedIntegrationProps#moreConfigurationSteps}
   * @see [values]{@link IOnUpdatedIntegrationProps#values}
   */
  onUpdatedIntegration(props: IOnUpdatedIntegrationProps): Promise<void>;
}

/**
 * A component to generate a configuration form for a given action and values.
 *
 * @see [action]{@link IWithConfigurationFormProps#action}
 * @see [moreConfigurationSteps]{@link IWithConfigurationFormProps#moreConfigurationSteps}
 * @see [values]{@link IWithConfigurationFormProps#values}
 */
export const WithConfigurationForm: React.FunctionComponent<
  IWithConfigurationFormProps
> = props => {
  const { t } = useTranslation('shared');

  const onSave = async (
    values: { [key: string]: string },
    actions: any
  ): Promise<void> => {
    await props.onUpdatedIntegration({
      values,
    });
    actions.setSubmitting(false);
  };

  let step = props.step.properties
    ? props.step
    : ALL_STEPS.find(s => s.stepKind === props.step.stepKind);

  let definition: { [key: string]: ConfigurationProperty };
  // if step is undefined, maybe we are dealing with an extension
  if (!step) {
    const steps = getActionSteps(props.step.action!.descriptor!);
    const actionStep = getActionStep(steps, 0);
    definition = getActionStepDefinition(actionStep);
    step = props.step;
  } else {
    definition = step.properties;
  }

  const initialValue = props.step.configuredProperties;
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
      key={step.id}
    >
      {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) =>
        props.children({
          form: (
            <IntegrationEditorForm
              i18nFormTitle={
                step!.description
                  ? `${step!.name} - ${step!.description}`
                  : step!.name
              }
              i18nNext={t('shared:Next')}
              isValid={isValid}
              isLoading={isSubmitting}
              submitForm={submitForm}
              handleSubmit={handleSubmit}
            >
              {fields}
            </IntegrationEditorForm>
          ),
          isSubmitting,
          isValid,
          submitForm,
        })
      }
    </AutoForm>
  );
};
