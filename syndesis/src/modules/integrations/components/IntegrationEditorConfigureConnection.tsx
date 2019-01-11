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
import { Breadcrumb, PageHeader } from '@syndesis/ui';
import { IntegrationActionConfigurationForm } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';

export interface IIntegrationEditorConfigureConnectionOnSaveProps {
  action: Action;
  configuredProperties: { [key: string]: string };
  moreSteps: boolean;
}

export interface IIntegrationEditorConfigureConnection {
  breadcrumb: JSX.Element[];
  disabled?: boolean;
  connection: ConnectionOverview;
  actionId: string;
  step: number;
  backLink: H.LocationDescriptor;
  onSave(
    configuredProperties: IIntegrationEditorConfigureConnectionOnSaveProps
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
      getActionStep(steps, this.props.step)
    );
    const moreSteps = this.props.step < steps.length - 1;
    const onSave = async (configuredProperties: { [key: string]: string }) => {
      this.props.onSave({
        action,
        configuredProperties,
        moreSteps,
      });
    };
    return (
      <>
        <PageHeader>
          <Breadcrumb>{this.props.breadcrumb}</Breadcrumb>

          <h1>{action.name}</h1>
          <p>{action.description}</p>
        </PageHeader>
        <AutoForm<{ [key: string]: string }>
          i18nRequiredProperty={'* Required field'}
          definition={definition}
          initialValue={{}}
          onSave={onSave}
        >
          {({ fields, handleSubmit }) => (
            <IntegrationActionConfigurationForm
              backLink={this.props.backLink}
              fields={fields}
              handleSubmit={handleSubmit}
              i18nBackLabel={'< Choose action'}
              i18nSubmitLabel={moreSteps ? 'Continue' : 'Done'}
            />
          )}
        </AutoForm>
      </>
    );
  }
}
