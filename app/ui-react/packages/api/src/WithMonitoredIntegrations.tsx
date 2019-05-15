import {
  Integration,
  IntegrationMonitoring,
  IntegrationWithMonitoring,
} from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IIntegrationsResponse, WithIntegrations } from './WithIntegrations';
import { IChangeEvent } from './WithServerEvents';

export interface IMonitoredIntegrationsResponse {
  items: IntegrationWithMonitoring[];
  totalCount: number;
}

export interface IWithMonitoredIntegrationsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IMonitoredIntegrationsResponse>): any;
}

export class WithMonitoredIntegrations extends React.Component<
  IWithMonitoredIntegrationsProps
> {
  public changeFilter(change: IChangeEvent) {
    return (
      change.kind === 'integration-deployment' ||
      change.kind === 'integration-deployment-state-details'
    );
  }
  public getMonitoredIntegrations(
    integrations: IIntegrationsResponse,
    response: IFetchState<IntegrationMonitoring[]>
  ) {
    return {
      items: integrations.items.map(
        (i: Integration): IntegrationWithMonitoring => ({
          integration: i,
          monitoring: response.data.find(
            (o: IntegrationMonitoring) => o.integrationId === i.id
          ),
        })
      ),
      totalCount: integrations.totalCount,
    } as IMonitoredIntegrationsResponse;
  }
  public render() {
    return (
      <WithIntegrations disableUpdates={this.props.disableUpdates}>
        {({ data: integrations, ...props }) => (
          <SyndesisFetch<IntegrationMonitoring[]>
            url={'/monitoring/integrations'}
            defaultValue={[]}
          >
            {({ read, response }) => {
              if (this.props.disableUpdates) {
                const data = this.getMonitoredIntegrations(
                  integrations,
                  response
                );
                return this.props.children({ ...props, data });
              }
              return (
                <ServerEventsContext.Consumer>
                  {({
                    registerChangeListener,
                    unregisterChangeListener,
                    registerMessageListener,
                    unregisterMessageListener,
                  }) => (
                    <WithChangeListener
                      read={read}
                      registerChangeListener={registerChangeListener}
                      unregisterChangeListener={unregisterChangeListener}
                      registerMessageListener={registerMessageListener}
                      unregisterMessageListener={unregisterMessageListener}
                      filter={this.changeFilter}
                    >
                      {() => {
                        const data = this.getMonitoredIntegrations(
                          integrations,
                          response
                        );
                        return this.props.children({ ...props, data });
                      }}
                    </WithChangeListener>
                  )}
                </ServerEventsContext.Consumer>
              );
            }}
          </SyndesisFetch>
        )}
      </WithIntegrations>
    );
  }
}
