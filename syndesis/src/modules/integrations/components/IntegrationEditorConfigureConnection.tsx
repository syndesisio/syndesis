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
import { PageHeader } from '@syndesis/ui';
import { IntegrationActionConfigurationForm } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';

export interface IOnUpdatedIntegrationProps {
  action: Action;
  moreConfigurationSteps: boolean;
  values: { [key: string]: string };
}

export interface IIntegrationEditorConfigureConnection {
  breadcrumb: JSX.Element;
  connection: ConnectionOverview;
  actionId: string;
  configurationStep: number;
  backLink: H.LocationDescriptor;
  initialValue?: { [key: string]: string };
  onUpdatedIntegration(props: IOnUpdatedIntegrationProps): Promise<void>;
}

export class IntegrationEditorConfigureConnection extends React.Component<
  IIntegrationEditorConfigureConnection
> {
  public static defaultProps = {
    initialValue: {},
  };

  public render() {
    const action = getActionById(
      getConnectorActions(getConnectionConnector(this.props.connection)),
      this.props.actionId
    );
    const steps = getActionSteps(getActionDescriptor(action));
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
      <>
        <PageHeader>
          {this.props.breadcrumb}
          <h1>{action.name}</h1>
          <p>{action.description}</p>
        </PageHeader>
        <AutoForm<{ [key: string]: string }>
          i18nRequiredProperty={'* Required field'}
          definition={definition}
          initialValue={this.props.initialValue!}
          onSave={onSave}
          key={this.props.configurationStep}
        >
          {({ fields, handleSubmit, isSubmitting }) => (
            <IntegrationActionConfigurationForm
              backLink={this.props.backLink}
              fields={fields}
              handleSubmit={handleSubmit}
              i18nBackLabel={'< Choose action'}
              i18nSubmitLabel={moreConfigurationSteps ? 'Continue' : 'Done'}
              disabled={isSubmitting}
            />
          )}
        </AutoForm>
      </>
    );
  }
}
