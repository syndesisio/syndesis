import { ListView } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

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
        data-testid={`integration-editor-actions-list-item-${toValidHtmlId(
          this.props.integrationName
        )}-list-item`}
        actions={this.props.actions}
        heading={this.props.integrationName}
        description={this.props.integrationDescription}
        stacked={true}
      />
    );
  }
}
