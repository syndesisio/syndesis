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
import { ConnectionOverview, Integration } from '@syndesis/models';
import { PageHeader } from '@syndesis/ui';
import { IntegrationActionConfigurationForm } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';
import {
  IWithAutoFormHelperOnUpdatedIntegrationProps,
  WithAutoFormHelper,
} from '../containers';

export interface IIntegrationEditorConfigureConnection {
  breadcrumb: JSX.Element;
  integration: Integration;
  connection: ConnectionOverview;
  actionId: string;
  configurationStep: number;
  backLink: H.LocationDescriptor;
  flow: number;
  flowStep: number;
  onUpdatedIntegration(
    props: IWithAutoFormHelperOnUpdatedIntegrationProps
  ): void;
}

export class IntegrationEditorConfigureConnection extends React.Component<
  IIntegrationEditorConfigureConnection
> {
  public render() {
    const action = getActionById(
      getConnectorActions(getConnectionConnector(this.props.connection)),
      this.props.actionId
    );
    const steps = getActionSteps(getActionDescriptor(action));
    const definition = getActionStepDefinition(
      getActionStep(steps, this.props.configurationStep)
    );
    const moreConfigurationSteps =
      this.props.configurationStep < steps.length - 1;
    return (
      <>
        <PageHeader>
          {this.props.breadcrumb}
          <h1>{action.name}</h1>
          <p>{action.description}</p>
        </PageHeader>
        <WithAutoFormHelper<{ [key: string]: string }>
          integration={this.props.integration}
          connection={this.props.connection}
          action={action}
          configurationStep={this.props.configurationStep}
          moreConfigurationSteps={moreConfigurationSteps}
          flow={this.props.flow}
          flowStep={this.props.flowStep}
          onUpdatedIntegration={this.props.onUpdatedIntegration}
        >
          {({ onSave }) => (
            <AutoForm<{ [key: string]: string }>
              i18nRequiredProperty={'* Required field'}
              definition={definition}
              initialValue={{}}
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
          )}
        </WithAutoFormHelper>
      </>
    );
  }
}
