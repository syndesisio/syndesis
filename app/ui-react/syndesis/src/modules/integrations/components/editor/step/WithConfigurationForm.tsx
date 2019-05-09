import { ALL_STEPS } from '@syndesis/api';
import { AutoForm } from '@syndesis/auto-form';
import { IConfigurationProperties, StepKind } from '@syndesis/models';
import { IntegrationEditorForm } from '@syndesis/ui';
import { toFormDefinition } from '@syndesis/utils';
import * as React from 'react';

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
export class WithConfigurationForm extends React.Component<
  IWithConfigurationFormProps
> {
  public static defaultProps = {
    initialValue: {},
  };

  public render() {
    const onSave = async (
      values: { [key: string]: string },
      actions: any
    ): Promise<void> => {
      await this.props.onUpdatedIntegration({
        values,
      });
      actions.setSubmitting(false);
    };
    // this can throw if the stepKind is not available for any given reason. Let
    // the error boundary catch and handle this.
    const step = this.props.step.properties
      ? this.props.step
      : ALL_STEPS.find(s => s.stepKind === this.props.step.stepKind)!;
    return (
      <AutoForm<{ [key: string]: string }>
        i18nRequiredProperty={'* Required field'}
        definition={toFormDefinition(
          step.properties as IConfigurationProperties
        )}
        initialValue={this.props.step.configuredProperties || {}}
        onSave={onSave}
        key={this.props.step.id}
      >
        {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) =>
          this.props.children({
            form: (
              <IntegrationEditorForm
                i18nFormTitle={`${step.name} - ${step.description}`}
                i18nNext={'Next'}
                isValid={isValid}
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
  }
}
