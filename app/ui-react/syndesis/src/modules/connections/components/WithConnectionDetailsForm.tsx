import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Connection } from '@syndesis/models';
import {
  ConnectionDetailsForm,
  IConnectionDetailsValidationResult,
} from '@syndesis/ui';
import * as React from 'react';

export interface IWithConnectionDetailsFormChildrenProps {
  /**
   * The form (embedded in the right UI elements)
   */
  form: JSX.Element;
  /**
   * `true` if the form contains valid values. Can be used to enable/disable the
   * submit button.
   */
  isValid: boolean;
  /**
   * `true` if the form is being submitted. Can be used to enable/disable the
   * submit button.
   */
  isSubmitting: boolean;
  /**
   * The callback to fire to submit the form.
   */
  submitForm(): any;
}

export interface IWithConnectionDetailsFormProps {
  /**
   * The connection object whose details are being shown in the form.
   */
  connection: Connection;

  /**
   * The localized validation message that is displayed when a required field does not have a value.
   */
  i18nRequiredPropertyMsg: string;

  /**
   * The localized text of the cancel button.
   */
  i18nCancelLabel: string;

  /**
   * The localized text of the edit button.
   */
  i18nEditLabel: string;

  /**
   * The localized text of the save button.
   */
  i18nSaveLabel: string;

  /**
   * The details form title.
   */
  i18nTitle: string;

  /**
   * The localized text of the validate button.
   */
  i18nValidateLabel: string;

  isValid: boolean;

  isWorking: boolean;

  validationResults?: IConnectionDetailsValidationResult[];
  /**
   * The render prop that will receive the ready-to-be-rendered form and some helpers.
   *
   * @see [form]{@link IWithConnectionDetailsFormChildrenProps#form}
   * @see [isValid]{@link IWithConnectionDetailsFormChildrenProps#isValid}
   * @see [isSubmitting]{@link IWithConnectionDetailsFormChildrenProps#isSubmitting}
   * @see [onSubmit]{@link IWithConnectionDetailsFormChildrenProps#submitForm}
   */
  children(props: IWithConnectionDetailsFormChildrenProps): any;

  /**
   * The callback invoked when the save button is clicked.
   */
  onSave(): Promise<boolean>;

  /**
   * The callback invoked when the validate button is clicked.
   */
  onValidate(): Promise<boolean>;
}

/**
 * A form component for showing a connection's details.
 */
export class WithConnectionDetailsForm extends React.Component<
  IWithConnectionDetailsFormProps
> {
  public render() {
    return (
      <AutoForm<{ [key: string]: string }>
        i18nRequiredProperty={this.props.i18nRequiredPropertyMsg}
        definition={
          this.props.connection.connector!.properties as IFormDefinition
        }
        initialValue={this.props.connection.configuredProperties!}
        onSave={this.props.onSave}
        validate={this.props.onValidate}
      >
        {({
          fields,
          handleSubmit,
          isSubmitting,
          isValid,
          submitForm,
          validateForm,
        }) =>
          this.props.children({
            form: (
              <ConnectionDetailsForm
                i18nCancelLabel={this.props.i18nCancelLabel}
                i18nEditLabel={this.props.i18nEditLabel}
                i18nSaveLabel={this.props.i18nSaveLabel}
                i18nTitle={this.props.i18nTitle}
                i18nValidateLabel={this.props.i18nValidateLabel}
                handleSubmit={handleSubmit}
                isValid={this.props.isValid}
                isWorking={this.props.isWorking}
                validationResults={this.props.validationResults}
                onValidate={validateForm}
              >
                {fields}
              </ConnectionDetailsForm>
            ),
            isSubmitting,
            isValid,
            submitForm,
          })
        }
      </AutoForm>
    );
  }
}
