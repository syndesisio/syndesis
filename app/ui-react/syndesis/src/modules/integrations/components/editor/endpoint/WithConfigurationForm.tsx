import {
  getActionById,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
  getConnectionConnector,
  getConnectorActions,
  WithActionDescriptor,
} from '@syndesis/api';
import { AutoForm } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { Action, ActionDescriptor, ConnectionOverview } from '@syndesis/models';
import {
  IntegrationEditorForm,
  IntegrationEditorNothingToConfigure,
  PageSectionLoader,
} from '@syndesis/ui';
import {
  getInitialValues,
  toFormDefinition,
  validateConfiguredProperties,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { ApiError } from '../../../../../shared';

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
   * the action object that has been configured.
   */
  action: Action;
  /**
   * true if the configuration is not complete because there are other steps,
   * false otherwise.
   * If true the form should be re-rendered with an incremented
   * [configurationStep]{@link IWithConfigurationFormProps#configurationStep}.
   */
  moreConfigurationSteps: boolean;
  /**
   * the configured values.
   */
  values: { [key: string]: string } | null;
}

export interface IWithConfigurationFormProps {
  /**
   * the connection object that contains the action with the provided
   * [actionId]{@link IWithConfigurationFormProps#actionId}. Used to retrieve
   * the form definition.
   */
  connection: ConnectionOverview;
  /**
   * the ID of the action that needs to be configured.
   */
  actionId: string;
  /**
   * for actions whose configuration must be performed in multiple steps,
   * indicates the current step.
   */
  configurationStep: number;
  /**
   * the values to assign to the form once rendered. These can come either from
   * an existing integration or from the [onUpdatedIntegration]{@link IWithConfigurationFormProps#onUpdatedIntegration}
   * callback.
   */
  initialValue?: { [key: string]: string };

  chooseActionHref: H.LocationDescriptor;

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

  public renderConfigurationForm(
    action: Action,
    descriptor: ActionDescriptor
  ): JSX.Element | null {
    try {
      const steps = getActionSteps(descriptor);
      const step = getActionStep(steps, this.props.configurationStep);
      const definition = getActionStepDefinition(step);
      const moreConfigurationSteps =
        this.props.configurationStep < steps.length - 1;
      const onSave = async (
        values: { [key: string]: string },
        actions: any
      ): Promise<void> => {
        await this.props.onUpdatedIntegration({
          action: { ...action, descriptor },
          moreConfigurationSteps,
          values,
        });
        actions.setSubmitting(false);
      };
      const key = JSON.stringify(definition);
      const allRequiredSet = validateConfiguredProperties(
        definition,
        this.props.initialValue
      );
      return (
        <AutoForm<{ [key: string]: string }>
          i18nRequiredProperty={'* Required field'}
          definition={toFormDefinition(definition)}
          initialValue={this.props.initialValue || getInitialValues(definition)}
          onSave={onSave}
          validate={(values: { [name: string]: any }): any => {
            return true;
          }}
          key={key}
        >
          {({ dirty, fields, handleSubmit, isValid, submitForm }) => (
            <>
              <IntegrationEditorForm
                i18nFormTitle={`${action.name} - ${action.description}`}
                i18nChooseAction={'Choose Action'}
                i18nNext={'Next'}
                isValid={allRequiredSet || isValid}
                submitForm={submitForm}
                handleSubmit={handleSubmit}
                chooseActionHref={this.props.chooseActionHref}
              >
                {fields}
              </IntegrationEditorForm>
            </>
          )}
        </AutoForm>
      );
    } catch (e) {
      return null;
    }
  }

  public renderNoPropertiesInfo(action: Action, descriptor: ActionDescriptor) {
    const submitForm = () => {
      this.props.onUpdatedIntegration({
        action: { ...action, descriptor },
        moreConfigurationSteps: false,
        values: null,
      });
    };
    return (
      <IntegrationEditorNothingToConfigure
        i18nAlert={'There are no properties to configure for this action.'}
        i18nChooseAction={'Choose Action'}
        i18nNext={'Next'}
        submitForm={submitForm}
        chooseActionHref={this.props.chooseActionHref}
      />
    );
  }

  public render() {
    const action = getActionById(
      getConnectorActions(getConnectionConnector(this.props.connection)),
      this.props.actionId
    );
    return (
      <WithActionDescriptor
        connectionId={this.props.connection.id!}
        actionId={action.id!}
        configuredProperties={{}}
      >
        {({ data, hasData, error }) => (
          <WithLoader
            error={error}
            loading={!hasData}
            loaderChildren={<PageSectionLoader />}
            errorChildren={<ApiError />}
          >
            {() =>
              this.renderConfigurationForm(action, data) ||
              this.renderNoPropertiesInfo(action, data)
            }
          </WithLoader>
        )}
      </WithActionDescriptor>
    );
  }
}
