import { WithIntegrationHelpers } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import * as React from 'react';

export interface IWithAutoFormHelperChildrenProps<T> {
  onSave(values: T, actions: any): void;
}

export interface IWithAutoFormHelperOnUpdatedIntegrationProps {
  moreConfigurationSteps: boolean;
  updatedIntegration: Integration;
  action: Action;
}

export interface IWithAutoFormHelperProps<T> {
  integration: Integration;
  connection: ConnectionOverview;
  action: Action;
  configurationStep: number;
  moreConfigurationSteps: boolean;
  flow: number;
  flowStep: number;
  onUpdatedIntegration(
    props: IWithAutoFormHelperOnUpdatedIntegrationProps
  ): void;
  children(props: IWithAutoFormHelperChildrenProps<T>): any;
}

export class WithAutoFormHelper<T> extends React.Component<
  IWithAutoFormHelperProps<T>
> {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ addConnection, updateConnection }) => {
          const onSave = async (values: T, actions: any): Promise<void> => {
            const updatedIntegration = await (this.props.configurationStep === 0
              ? addConnection
              : updateConnection)(
              this.props.integration,
              this.props.connection,
              this.props.action,
              this.props.flow,
              this.props.flowStep,
              values
            );
            actions.setSubmitting(false);
            this.props.onUpdatedIntegration({
              action: this.props.action,
              moreConfigurationSteps: this.props.moreConfigurationSteps,
              updatedIntegration,
            });
          };
          return this.props.children({
            onSave,
          });
        }}
      </WithIntegrationHelpers>
    );
  }
}
