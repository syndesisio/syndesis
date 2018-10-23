import * as React from 'react';
import {
  IIntegration,
  IIntegrationMonitoring,
  IIntegrationsRawResponse,
  IMonitoredIntegration,
  SyndesisRest
} from './index';

export interface IMonitoredIntegrationsResponse {
  integrations: IMonitoredIntegration[];
  integrationsCount: number;
}

export interface IWithMonitoredIntegrationsProps {
  children(props: IMonitoredIntegrationsResponse): any;
}

export class WithMonitoredIntegrations extends React.Component<IWithMonitoredIntegrationsProps> {
  public render() {
    return (
      <SyndesisRest<IIntegrationsRawResponse> url={'/api/v1/integrations'} poll={5000}>
        {asyncIntegrations =>
          <SyndesisRest<IIntegrationMonitoring[]> url={'/api/v1/monitoring/integrations'} poll={5000}>
            {asyncMonitoring => {
              const integrations = asyncIntegrations.data && asyncIntegrations.data.items
                ? asyncIntegrations.data.items
                : [];
              const integrationsCount = asyncIntegrations.data && asyncIntegrations.data.totalCount
                ? asyncIntegrations.data.totalCount
                : 0;
              const monitorings = asyncMonitoring.data || [];
              return this.props.children({
                integrations: integrations.map((i: IIntegration): IMonitoredIntegration => ({
                  integration: i,
                  monitoring: monitorings.find((m: IIntegrationMonitoring) => m.integrationId === i.id)
                })),
                integrationsCount
              });
            }}
          </SyndesisRest>
        }
      </SyndesisRest>
    )
  }
}