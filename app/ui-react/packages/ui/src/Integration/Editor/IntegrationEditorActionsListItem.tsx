import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationEditorActionsListItemProps {
  integrationName: string;
  integrationDescription: string;
  actions: any;
}

export class IntegrationEditorActionsListItem extends React.Component<
  IIntegrationEditorActionsListItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={this.props.actions}
        heading={this.props.integrationName}
        description={this.props.integrationDescription}
        stacked={true}
      />
    );
  }
}
