import { ListView } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

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
        data-testid={`integration-editor-connections-list-item-${toValidHtmlId(
          this.props.integrationName
        )}-list-item`}
        actions={this.props.actions}
        heading={this.props.integrationName}
        description={this.props.integrationDescription}
        leftContent={this.props.icon}
        stacked={true}
      />
    );
  }
}
