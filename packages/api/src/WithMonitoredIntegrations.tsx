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

export interface IMonitoredIntegrationsResponse {
  items: IntegrationWithMonitoring[];
  totalCount: number;
}

export interface IWithMonitoredIntegrationsProps {
  children(props: IRestState<IMonitoredIntegrationsResponse>): any;
}

export class WithMonitoredIntegrations extends React.Component<
  IWithMonitoredIntegrationsProps
> {
  public render() {
    return (
      <WithIntegrations>
        {({ data: integrations, ...props }) => (
          <SyndesisRest<IntegrationMonitoring[]>
            url={'/monitoring/integrations'}
            poll={5000}
            defaultValue={[]}
          >
            {({ data: monitorings }) => {
              return this.props.children({
                ...props,
                data: {
                  items: integrations.items.map(
                    (i: Integration): IntegrationWithOverview => ({
                      integration: i,
                      overview: monitorings.find(
                        (o: IntegrationMonitoring) => o.id === i.id
                      ),
                    })
                  ),
                  totalCount: integrations.totalCount,
                },
              });
            }}
          </SyndesisRest>
        )}
      </WithIntegrations>
    );
  }
}
