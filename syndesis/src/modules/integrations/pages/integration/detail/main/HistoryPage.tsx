import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';

interface IHistoryPageParams {
  integrationId: string;
}

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects an integrationId in the URL
 *
 */
export class HistoryPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IHistoryPageParams, null>>
        {({ integrationId }, {}) => {
          //
        }}
      </WithRouteData>
    );
  }
}
