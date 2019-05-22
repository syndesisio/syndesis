import { IntegrationOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IIntegrationsResponse {
  items: IntegrationOverview[];
  totalCount: number;
}

export interface IWithIntegrationsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IIntegrationsResponse>): any;
}

export class WithIntegrations extends React.Component<IWithIntegrationsProps> {
  public changeFilter(change: IChangeEvent) {
    return (
      change.kind === 'integration' || change.kind === 'integration-deployment'
    );
  }

  public render() {
    return (
      <SyndesisFetch<IIntegrationsResponse>
        url={'/integrations?per_page=50'}
        defaultValue={{ items: [], totalCount: 0 }}
      >
        {({ read, response }) =>
          this.props.disableUpdates ? (
            this.props.children(response)
          ) : (
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
                  {() => this.props.children(response)}
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          )
        }
      </SyndesisFetch>
    );
  }
}
