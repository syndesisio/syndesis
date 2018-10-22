import * as React from 'react';
import { IntegrationsListView } from '../../components';
import { WithConnections, WithIntegrationsMetrics, WithMonitoredIntegrations, WithRouter } from '../../containers';

export const IntegrationsPage = () => (
  <WithMonitoredIntegrations>
    {({integrationsCount, integrations}) =>
      <WithIntegrationsMetrics>
        {metrics =>
          <WithConnections>
            {({connections}) =>
              <WithRouter>
                {({match}) =>
                  <IntegrationsListView
                    match={match}
                    monitoredIntegrations={integrations}
                    integrationsCount={integrationsCount}
                    connections={connections}
                    metrics={metrics}
                  />
                }
              </WithRouter>
            }
          </WithConnections>
        }
      </WithIntegrationsMetrics>
    }
  </WithMonitoredIntegrations>
);