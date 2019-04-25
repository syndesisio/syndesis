import { ListView, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  actions: any;
  /**
   * The last date the integration was updated.
   */
  updatedAt?: number;
  /**
   * The version of the integration deployment.
   */
  version?: number;
  /**
   * The localized text for displaying the last published date.
   */
  i18nTextLastPublished?: string;
  /**
   * The localized text used to display the version of the integration.
   */
  i18nTextVersion?: string;
}

export class IntegrationDetailHistoryListViewItem extends React.Component<
  IIntegrationDetailHistoryListViewItemProps
> {
  public render() {
    return (
      <ListViewItem
        actions={this.props.actions}
        heading={
          <span>
            {<span>{this.props.i18nTextVersion}:</span>} {this.props.version}
          </span>
        }
        additionalInfo={[
          <ListViewInfoItem key={1}>
            {this.props.i18nTextLastPublished}
            {this.props.updatedAt}
          </ListViewInfoItem>,
        ]}
        leftContent={
          // TODO: If first item and current deployment, show status icon
          <ListView.Icon
            type="pf"
            name="ok"
            size="sm"
            className="list-view-pf-icon-success"
          />
        }
        stacked={false}
      />
    );
  }
}
