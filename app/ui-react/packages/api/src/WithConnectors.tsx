import { IConnector } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { transformConnectorResponse } from './useConnector';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IConnectorsFetchResponse {
  readonly items: IConnector[];
  readonly totalCount: number;
}

export interface IConnectorsResponse {
  readonly connectorsForDisplay: IConnector[];
  readonly dangerouslyUnfilteredConnections: IConnector[];
  readonly totalCount: number;
}

export interface IWithConnectorsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IConnectorsResponse>): any;
}

export function getConnectorsForDisplay(connectors: IConnector[]) {
  return connectors.filter(
    c => !c.metadata || !c.metadata['hide-from-connection-pages']
  );
}

export function transformConnectorsResponse(
  response: IFetchState<IConnectorsFetchResponse>
): IFetchState<IConnectorsResponse> {
  const connectors = response.data.items.map(transformConnectorResponse);

  return {
    ...response,
    data: {
      connectorsForDisplay: getConnectorsForDisplay(connectors),
      dangerouslyUnfilteredConnections: connectors,
      totalCount: response.data.totalCount,
    },
  };
}

export class WithConnectors extends React.Component<IWithConnectorsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connector');
  }
  public render() {
    return (
      <SyndesisFetch<IConnectorsFetchResponse>
        url={'/connectors?per_page=50'}
        defaultValue={{
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(transformConnectorsResponse(response));
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
                  {() =>
                    this.props.children(transformConnectorsResponse(response))
                  }
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          );
        }}
      </SyndesisFetch>
    );
  }
}
