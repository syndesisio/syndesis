import { WithConnectionHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Connector } from '@syndesis/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';
import * as React from 'react';

export interface IWithConfigurationFormChildrenProps {
  /**
   * the form (embedded in the right UI elements)
   */
  fields: JSX.Element;
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

export interface IWithConfigurationFormProps {
  /**
   * the connection object that contains the action with the provided
   * [actionId]{@link IWithConfigurationFormProps#actionId}. Used to retrieve
   * the form definition.
   */
  connector: Connector;
  /**
   * the values to assign to the form once rendered. These can come either from
   * an existing integration or from the [onSave]{@link IWithConfigurationFormProps#onSave}
   * callback.
   */
  initialValue?: { [key: string]: string };

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
   */
  onSave(props: { [key: string]: string }, action: any): any;
}

/**
 * A component to generate a configuration form for a given action and values.
 *
 * @see [action]{@link IWithConfigurationFormProps#action}
 * @see [moreConfigurationSteps]{@link IWithConfigurationFormProps#moreConfigurationSteps}
 * @see [values]{@link IWithConfigurationFormProps#values}
 */
export class WithConfigurationForm extends React.Component<
  IWithConfigurationFormProps
> {
  public static defaultProps = {
    initialValue: {},
  };

  public render() {
    const definition = this.props.connector.properties;
    return (
      <WithConnectionHelpers>
        {({ validateConfiguration }) => {
          let shouldValidateAgainstBackend = false;
          let validationResults: IConnectorConfigurationFormValidationResult[] = [];
          const validateFormAgainstBackend = async (values: {
            [key: string]: string;
          }) => {
            validationResults = [];
            if (shouldValidateAgainstBackend) {
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
            }
          };

          return (
            <AutoForm<{ [key: string]: string }>
              i18nRequiredProperty={'* Required field'}
              definition={definition as IFormDefinition}
              initialValue={this.props.initialValue!}
              validate={validateFormAgainstBackend}
              onSave={this.props.onSave}
            >
              {({
                fields,
                handleSubmit,
                isSubmitting,
                isValid,
                isValidating,
                submitForm,
                validateForm,
                values,
              }) => {
                const enableValidationAgainstBackend = async () => {
                  shouldValidateAgainstBackend = true;
                  await validateForm();
                  shouldValidateAgainstBackend = false;
                };

                return this.props.children({
                  fields,
                  handleSubmit,
                  isSubmitting,
                  isValid,
                  isValidating,
                  submitForm,
                  validateForm: enableValidationAgainstBackend,
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
