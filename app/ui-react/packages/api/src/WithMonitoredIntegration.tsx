import {
  IIntegrationOverviewWithDraft,
  IntegrationMonitoring,
  IntegrationWithMonitoring,
} from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { WithIntegration } from './WithIntegration';
import { IChangeEvent } from './WithServerEvents';

export interface IWithMonitoredIntegrationProps {
  integrationId: string;
  disableUpdates?: boolean;
  initialValue?: IIntegrationOverviewWithDraft;
  children(props: IFetchState<IntegrationWithMonitoring>): any;
}

/**
 * A component that fetches the integration with the specified identifier.
 * @see [integrationId]{@link IWithIntegrationProps#integrationId}
 */
export class WithMonitoredIntegration extends React.Component<
  IWithMonitoredIntegrationProps
> {
  public constructor(props: IWithMonitoredIntegrationProps) {
    super(props);
    this.changeFilter = this.changeFilter.bind(this);
  }
  public changeFilter(change: IChangeEvent) {
    return (
      change.kind.startsWith('integration') &&
      change.id.startsWith(this.props.integrationId)
    );
  }
  public getMonitoredIntegration(
    integration: IIntegrationOverviewWithDraft,
    response: IFetchState<IntegrationMonitoring[]>
  ) {
    return {
      integration,
      monitoring: response.data.find(
        (o: IntegrationMonitoring) => o.integrationId === integration.id
      ),
    };
  }
  public render() {
    return (
      <WithIntegration
        integrationId={this.props.integrationId}
        initialValue={this.props.initialValue}
        disableUpdates={this.props.disableUpdates}
      >
        {({ data: integration, ...props }) => (
          <SyndesisFetch<IntegrationMonitoring[]>
            url={'/monitoring/integrations'}
            defaultValue={[]}
          >
            {({ read, response }) => {
              if (this.props.disableUpdates) {
                const data = this.getMonitoredIntegration(
                  integration,
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
                        const data = this.getMonitoredIntegration(
                          integration,
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
      </WithIntegration>
    );
  }
}
