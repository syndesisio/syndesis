import { Integration } from '@syndesis/models';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { IntegrationDetailNavBar } from '../../shared/IntegrationDetailNavBar';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IMetricsPageParams {
  integrationId: string;
}

export interface IMetricsPageState {
  integration: Integration;
}

/**
 * This page shows the second tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class MetricsPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IMetricsPageParams, IMetricsPageState>>
        {({ integrationId }, { integration }, { history }) => {
          return (
            <div>
              <Translation ns={['integration', 'shared']}>
                {t => <IntegrationDetailNavBar integration={integration} />}
              </Translation>
              <p>This is the Integration Detail Metrics page.</p>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
