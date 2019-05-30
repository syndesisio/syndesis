import { ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  /**
   * If the integration is a draft, it renders a set of actionable buttons,
   * labeled 'Edit' and 'Publish'
   * If the integration is not a draft, it renders a dropdown actions menu
   * based on the IntegrationActions component
   */
  actions: any;
  /**
   * The current state of the integration.
   */
  currentState: string;
  /**
   * The last date the integration was updated.
   */
  updatedAt?: string;
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

const states = {
  Error: <ListView.Icon type="pf" name="error-circle-o" />,
  Pending: <ListView.Icon name="blank" />,
  Published: <ListView.Icon type="pf" name="ok" />,
  Unpublished: <ListView.Icon name="blank" />,
};

export class IntegrationDetailHistoryListViewItem extends React.Component<
  IIntegrationDetailHistoryListViewItemProps
> {
  public render() {
    function getIntegrationState(currentState: string) {
      return states[currentState] || null;
    }
    return (
      <ListViewItem
        actions={this.props.actions}
        heading={
          <>
            {this.props.i18nTextVersion}: {this.props.version}
          </>
        }
        description={
          <>
            {this.props.i18nTextLastPublished}
            {this.props.updatedAt}
          </>
        }
        leftContent={getIntegrationState(this.props.currentState!)}
        stacked={false}
      />
    );
  }
}
