import {
  Integration,
  IntegrationMonitoring,
  IntegrationWithMonitoring,
  IntegrationWithOverview,
} from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { SyndesisRest } from './SyndesisRest';
import { WithIntegrations } from './WithIntegrations';
import { WithPolling } from './WithPolling';

export interface IMonitoredIntegrationsResponse {
  items: IntegrationWithMonitoring[];
  totalCount: number;
}

export interface IWithMonitoredIntegrationsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IMonitoredIntegrationsResponse>): any;
}

export class WithMonitoredIntegrations extends React.Component<
  IWithMonitoredIntegrationsProps
> {
  public render() {
    return (
      <WithIntegrations disableUpdates={this.props.disableUpdates}>
        {({ data: integrations, ...props }) => (
          <SyndesisRest<IntegrationMonitoring[]>
            url={'/monitoring/integrations'}
            defaultValue={[]}
          >
            {({ read, response }) => {
              const data = {
                items: integrations.items.map(
                  (i: Integration): IntegrationWithOverview => ({
                    integration: i,
                    overview: response.data.find(
                      (o: IntegrationMonitoring) => o.id === i.id
                    ),
                  })
                ),
                totalCount: integrations.totalCount,
              };
              if (this.props.disableUpdates) {
                return this.props.children({ ...props, data });
              }
              return (
                <WithPolling read={read} polling={5000}>
                  {() => this.props.children({ ...props, data })}
                </WithPolling>
              );
            }}
          </SyndesisRest>
        )}
      </WithIntegrations>
    );
  }
}
