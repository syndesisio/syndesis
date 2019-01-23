import {
  getActionById,
  getActionDescriptor,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
  getConnectionConnector,
  getConnectorActions,
} from '@syndesis/api';
import { AutoForm } from '@syndesis/auto-form';
import { Action, ConnectionOverview } from '@syndesis/models';
import { IntegrationActionConfigurationCard } from '@syndesis/ui';
import * as React from 'react';

export interface IWithConfigurationFormChildrenProps {
  form: JSX.Element;
  isValid: boolean;
  isSubmitting: boolean;
  onSubmit(): any;
}

export interface IOnUpdatedIntegrationProps {
  action: Action;
  moreConfigurationSteps: boolean;
  values: { [key: string]: string } | null;
}

export interface IWithConfigurationFormProps {
  connection: ConnectionOverview;
  actionId: string;
  configurationStep: number;
  initialValue?: { [key: string]: string };
  children(props: IWithConfigurationFormChildrenProps): any;
  onUpdatedIntegration(props: IOnUpdatedIntegrationProps): Promise<void>;
}

export class WithConfigurationForm extends React.Component<
  IWithConfigurationFormProps
> {
  public static defaultProps = {
    initialValue: {},
  };

  constructor(props: IWithConfigurationFormProps) {
    super(props);
  }

  public renderConfigurationForm(action: Action): JSX.Element | null {
    try {
      const descriptor = getActionDescriptor(action);
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
          action,
          moreConfigurationSteps,
          values,
        });
        actions.setSubmitting(false);
      };
      return (
        <AutoForm<{ [key: string]: string }>
          i18nRequiredProperty={'* Required field'}
          definition={definition}
          initialValue={this.props.initialValue!}
          onSave={onSave}
          key={this.props.configurationStep}
        >
          {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) =>
            this.props.children({
              form: (
                <>
                  <div className="container-fluid">
                    <h1>Configure action</h1>
                    <p>
                      Fill in the required information for the selected action.
                    </p>
                  </div>
                  <form
                    className="form-horizontal required-pf"
                    role="form"
                    onSubmit={handleSubmit}
                  >
                    <IntegrationActionConfigurationCard
                      content={fields}
                      title={`${action.name} - ${action.description}`}
                    />
                  </form>
                </>
              ),
              isSubmitting,
              isValid,
              onSubmit: submitForm,
            })
          }
        </AutoForm>
      );
    } catch (e) {
      return null;
    }
  }

  public renderNoPropertiesInfo(action: Action) {
    const onSubmit = () => {
      this.props.onUpdatedIntegration({
        action,
        moreConfigurationSteps: false,
        values: null,
      });
    };
    return this.props.children({
      form: (
        <>
          <div className="container-fluid">
            <h1>Configure action</h1>
            <p>Fill in the required information for the selected action.</p>
          </div>
          <IntegrationActionConfigurationCard
            content={
              <p className="alert alert-info">
                <span className="pficon pficon-info" />
                There are no properties to configure for this action.
              </p>
            }
            title={`${action.name} - ${action.description}`}
          />
        </>
      ),
      isSubmitting: false,
      isValid: true,
      onSubmit,
    });
  }

  public render() {
    const action = getActionById(
      getConnectorActions(getConnectionConnector(this.props.connection)),
      this.props.actionId
    );
    return (
      this.renderConfigurationForm(action) ||
      this.renderNoPropertiesInfo(action)
    );
  }
}
