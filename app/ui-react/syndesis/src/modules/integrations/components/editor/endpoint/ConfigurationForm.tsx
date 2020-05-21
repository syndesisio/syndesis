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
    Pick<IWithConfigurationFormProps, 'isBackAllowed'>,
    Pick<IWithConfigurationFormProps, 'oldAction'>,
    Pick<IWithConfigurationFormProps, 'onUpdatedIntegration'>,
    Pick<IWithConfigurationFormProps, 'chooseActionHref'> {
  action: Action;
  descriptor: ActionDescriptor;
  /**
   * A function that accepts the definition
   * and, depending on the action ID (e.g. webhook), returns
   * itself or a mutated version of itself with its respective
   * error keys.
   * @param definition
   */
  definitionCustomizer: (
    definition: IConfigurationProperties
  ) => IConfigurationProperties;
  children: any;
  isBackAllowed: boolean;
}

export const ConfigurationForm: React.FunctionComponent<IConfigurationFormProps> = ({
  action,
  configurationPage,
  descriptor,
  definitionCustomizer,
  isBackAllowed,
  initialValue,
  oldAction,
  chooseActionHref,
  onUpdatedIntegration,
  children,
}) => {
  const { t } = useTranslation('shared');
  const [error, setError] = React.useState();
  try {
    const propertyDefinitionSteps = getActionSteps(descriptor);
    const propertyDefinitionStep = getActionStep(
      propertyDefinitionSteps,
      configurationPage
    );

    /**
     * Fetches the form definition (using the property
     * definition step) from the API.
     */
    const actionStepDefinition = getActionStepDefinition(
      propertyDefinitionStep
    );

    /**
     * Definition that is later mapped to an AutoForm
     * object.
     */
    const definition = definitionCustomizer(actionStepDefinition);

    const moreConfigurationSteps =
      configurationPage < propertyDefinitionSteps.length - 1;
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
            : { ...action, descriptor };
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
      typeof propertyDefinitionStep.description === 'undefined'
        ? action.name
        : `${action.name} - ${propertyDefinitionStep.description}`;
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
        {({ fields, handleSubmit, isValid, isSubmitting, submitForm }) => {
          return (
            <IntegrationEditorForm
              i18nFormTitle={formTitle}
              i18nBackAction={'Choose Action'}
              i18nNext={'Next'}
              isBackAllowed={isBackAllowed}
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
          );
        }}
      </AutoForm>
    );
  } catch (e) {
    return children;
  }
};
