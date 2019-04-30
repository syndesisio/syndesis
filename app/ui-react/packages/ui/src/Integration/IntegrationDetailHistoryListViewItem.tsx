import { ListView, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  /**
   * Dropdown actions menu
   */
  actions: any;
  /**
   * The current state of the integration.
   */
  currentState?: string;
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
    function getIntegrationState(currentState: string) {
      let states = {
        Pending: null,
        Published: () => {
          return (
            <ListView.Icon
              type="pf"
              name="ok"
              size={'sm'}
              className="list-view-pf-icon-success"
            />
          );
        },
        Unpublished: null,
        Error: () => {
          return (
            <ListView.Icon
              type="pf"
              size={'sm'}
              className="pficon pficon-error-circle-o"
            />
          );
        },
      };

      return states[currentState] || null;
    }

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
        leftContent={getIntegrationState(this.props.currentState!)}
        stacked={false}
      />
    );
  }
}
