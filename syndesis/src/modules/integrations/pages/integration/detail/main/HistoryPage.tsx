// import { WithIntegrations } from '@syndesis/api';
import { Integration } from '@syndesis/models';
// import { IntegrationDetailHistoryListView, IntegrationDetailHistoryListViewItem } from '@syndesis/ui';
// import { Loader } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IHistoryPageParams {
  integration: Integration;
  integrationId: string;
}

export interface IHistoryPageState {
  integration: Integration;
}

// const integrationHistoryItems: IntegrationDetailHistoryListViewItem[];

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects an integrationId in the URL
 *
 */
export class HistoryPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IHistoryPageParams, IHistoryPageState>>
        {({ integrationId }, { integration }, { history }) => {
          return (
            <div>
              <p>This is the Integration Detail History page.</p>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
