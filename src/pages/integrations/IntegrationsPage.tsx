import * as React from 'react';
import { IntegrationsListView } from '../../components';
import { WithIntegrationsMetrics, WithMonitoredIntegrations } from '../../containers';

export const IntegrationsPage = () => (
  <WithMonitoredIntegrations>
    {({integrationsCount, integrations}) =>
      <WithIntegrationsMetrics>
        {metrics =>
          <IntegrationsListView
            monitoredIntegrations={integrations}
            integrationsCount={integrationsCount}
            metrics={metrics}
          />
        }
      </WithIntegrationsMetrics>
    }
  </WithMonitoredIntegrations>
);