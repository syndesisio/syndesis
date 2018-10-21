import * as React from 'react';
import { Dashboard } from '../components';
import { WithConnections, WithIntegrations, WithIntegrationsMetrics } from '../containers';

export const DashboardPage = () => (
  <WithIntegrations>
    {({integrationsCount, integrations}) =>
      <WithIntegrationsMetrics>
        {metrics =>
          <WithConnections>
            {({connectionsCount, connections}) =>
              <Dashboard
                integrations={integrations}
                integrationsCount={integrationsCount}
                connections={connections}
                connectionsCount={connectionsCount}
                metrics={metrics}
              />
            }
          </WithConnections>
        }
      </WithIntegrationsMetrics>
    }
  </WithIntegrations>
);