import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationEditorConnectionsListItemProps {
  integrationName: string;
  integrationDescription: string;
  icon: any;
  actions: any;
}

export class IntegrationEditorConnectionsListItem extends React.Component<
  IIntegrationEditorConnectionsListItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={this.props.actions}
        heading={this.props.integrationName}
        description={this.props.integrationDescription}
        leftContent={this.props.icon}
        stacked={true}
      />
    );
  }
}
