import * as React from 'react';
import { IntegrationsListView } from '../../components';
import { WithIntegrationsMetrics, WithMonitoredIntegrations, WithRouter } from '../../containers';

export const IntegrationsPage = () => (
  <WithMonitoredIntegrations>
    {({integrationsCount, integrations}) =>
      <WithIntegrationsMetrics>
        {metrics =>
          <WithRouter>
            {({match}) =>
              <IntegrationsListView
                match={match}
                monitoredIntegrations={integrations}
                integrationsCount={integrationsCount}
                metrics={metrics}
              />
            }
          </WithRouter>
        }
      </WithIntegrationsMetrics>
    }
  </WithMonitoredIntegrations>
);