import { WithConnectionHelpers } from '@syndesis/api';
import { AutoForm, IFormValue } from '@syndesis/auto-form';
import { Connector } from '@syndesis/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';
import {
  allFieldsRequired,
  applyInitialValues,
  getRequiredStatusText,
  toFormDefinition,
  validateRequiredProperties,
} from '@syndesis/utils';
import * as React from 'react';
import i18n from '../../../i18n';

export interface IWithConnectorFormChildrenProps {
  /**
   * the form (embedded in the right UI elements)
   */
  fields: JSX.Element;
  /**
   * true if the form has been modified.
   */
  dirty: boolean;
  /**
   * true if the form contains valid values. Can be used to enable/disable the
   * submit button.
   */
  isValid: boolean;
  /**
   * true if the form is being validated. Can be used to enable/disable the
   * validate button.
   */
  isValidating: boolean;
  /**
   * true if the form is being submitted. Can be used to enable/disable the
   * submit button.
   */
  isSubmitting: boolean;
  /**
   * the current values of the form fields
   */
  values: { [key: string]: string };

  validationResults: IConnectorConfigurationFormValidationResult[];

  resetForm: (nextValues?: any) => void;

  handleSubmit: (e?: any) => void;
  /**
   * the callback to trigger to validate the form against the backend.
   */
  validateForm(): any;
  /**
   * the callback to trigger to submit the form.
   */
  submitForm(): any;
}

export interface IWithConnectorFormProps {
  /**
   * the connection object that contains the action with the provided
   * [actionId]{@link IWithConnectorFormProps#actionId}. Used to retrieve
   * the form definition.
   */
  connector: Connector;
  /**
   * the values to assign to the form once rendered. These can come either from
   * an existing integration or from the [onSave]{@link IWithConnectorFormProps#onSave}
   * callback.
   */
  initialValue?: { [key: string]: string };

  /**
   * true to have the fields rendered as read-only.
   */
  disabled?: boolean;

  /**
   * the render prop that will receive the ready-to-be-rendered form and some
   * helpers.
   *
   * @see [form]{@link IWithConnectorFormChildrenProps#form}
   * @see [isValid]{@link IWithConnectorFormChildrenProps#isValid}
   * @see [isSubmitting]{@link IWithConnectorFormChildrenProps#isSubmitting}
   * @see [onSubmit]{@link IWithConnectorFormChildrenProps#submitForm}
   */
  children(props: IWithConnectorFormChildrenProps): any;

  /**
   * the callback that is fired after the form submit with valid values.
   *
   * @see [action]{@link IOnUpdatedIntegrationProps#action}
   */
  onSave(props: { [key: string]: string }, action: any): any;
}

/**
 * A component to generate a configuration form for a given action and values.
 *
 * @see [action]{@link IWithConnectorFormProps#action}
 * @see [moreConfigurationSteps]{@link IWithConnectorFormProps#moreConfigurationSteps}
 * @see [values]{@link IWithConnectorFormProps#values}
 */
export class WithConnectorForm extends React.Component<
  IWithConnectorFormProps
> {
  public static defaultProps = {
    initialValue: {},
  };

  public render() {
    const definition = Object.keys(this.props.connector.properties!).reduce(
      (def, key) => {
        const d = this.props.connector.properties![key];
        def[key] = {
          ...d,
          disabled: this.props.disabled,
        };
        return def;
      },
      {}
    );
    return (
      <WithConnectionHelpers>
        {({ validateConfiguration }) => {
          let validationResults: IConnectorConfigurationFormValidationResult[] = [];
          const validateFormAgainstBackend = async (values: {
            [key: string]: string;
          }) => {
            validationResults = [];
            const errors: { [key: string]: string } = {};
            const status = await validateConfiguration(
              this.props.connector.id!,
              values
            );
            (
              (
                status.find(obj => obj.scope === 'PARAMETERS') || {
                  errors: [],
                }
              ).errors || []
            ).forEach(obj => {
              obj.parameters.forEach(p => (errors[p] = obj.description));
            });
            validationResults = (
              (
                status.find(obj => obj.scope === 'CONNECTIVITY') || {
                  errors: [],
                }
              ).errors || []
            ).map(
              obj =>
                ({
                  message: obj.description,
                  type: 'error',
                } as IConnectorConfigurationFormValidationResult)
            );
            if (Object.keys(errors).length) {
              throw errors;
            }
            if (validationResults.length === 0) {
              validationResults = [
                {
                  message: `${
                    this.props.connector.name
                  } has been successfully validated`,
                  type: 'success',
                } as IConnectorConfigurationFormValidationResult,
              ];
            }
            return validationResults;
          };
          const initialValue = applyInitialValues(
            definition,
            this.props.initialValue!
          );
          const requiredPrompt = getRequiredStatusText(
            definition,
            i18n.t('shared:AllFieldsRequired'),
            i18n.t('shared:FieldsMarkedWithStarRequired'),
            ''
          );
          return (
            <AutoForm<IFormValue>
              i18nRequiredProperty={'* Required field'}
              definition={toFormDefinition(definition)}
              i18nFieldsStatusText={requiredPrompt}
              allFieldsRequired={allFieldsRequired(definition)}
              initialValue={initialValue!}
              validate={(values: IFormValue) =>
                validateRequiredProperties(
                  definition,
                  (name: string) => `${name} is required`,
                  values
                )
              }
              onSave={this.props.onSave}
            >
              {({
                fields,
                handleSubmit,
                isSubmitting,
                dirty,
                isValid,
                isValidating,
                resetForm,
                submitForm,
                validateForm,
                values,
              }) => {
                return this.props.children({
                  dirty,
                  fields,
                  handleSubmit,
                  isSubmitting,
                  isValid,
                  isValidating,
                  resetForm,
                  submitForm,
                  validateForm: () => validateFormAgainstBackend(values),
                  validationResults,
                  values,
                });
              }}
            </AutoForm>
          );
        }}
      </WithConnectionHelpers>
    );
  }
}
