import { IConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { transformConnectionResponse } from './useConnection';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export function getConnectionsForDisplay(connections: IConnectionOverview[]) {
  return connections.filter(
    c => !c.metadata || !c.metadata['hide-from-connection-pages']
  );
}

export function getConnectionsWithFromAction(
  connections: IConnectionOverview[]
) {
  return connections.filter(connection => {
    if (!connection.connector) {
      // safety net
      return true;
    }
    return connection.connector.actions.some(action => {
      return action.pattern === 'From';
    });
  });
}

export function getConnectionsWithToAction(connections: IConnectionOverview[]) {
  return connections.filter(connection => {
    if (!connection.connector) {
      // safety net
      return true;
    }
    if (connection.connectorId === 'api-provider') {
      // api provider can be used only for From actions
      return false;
    }
    return connection.connector.actions.some(action => {
      return action.pattern === 'To';
    });
  });
}

export interface IConnectionsFetchResponse {
  readonly items: IConnectionOverview[];
  readonly totalCount: number;
}

export interface IConnectionsResponse {
  readonly connectionsForDisplay: IConnectionOverview[];
  readonly connectionsWithToAction: IConnectionOverview[];
  readonly connectionsWithFromAction: IConnectionOverview[];
  readonly dangerouslyUnfilteredConnections: IConnectionOverview[];
  readonly totalCount: number;
}

export interface IWithConnectionsProps {
  disableUpdates?: boolean;
  debounceWait?: number;
  children(props: IFetchState<IConnectionsResponse>): any;
}

export function transformResponse(
  response: IFetchState<IConnectionsFetchResponse>
): IFetchState<IConnectionsResponse> {
  const connections = response.data.items.map(transformConnectionResponse);
  return {
    ...response,
    data: {
      connectionsForDisplay: getConnectionsForDisplay(connections),
      connectionsWithFromAction: getConnectionsWithFromAction(connections),
      connectionsWithToAction: getConnectionsWithToAction(connections),
      dangerouslyUnfilteredConnections: connections,
      totalCount: response.data.totalCount,
    },
  };
}

export class WithConnections extends React.Component<IWithConnectionsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connection');
  }

  public render() {
    return (
      <SyndesisFetch<IConnectionsFetchResponse>
        url={'/connections?per_page=50'}
        defaultValue={{
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(transformResponse(response));
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
                  debounceWait={this.props.debounceWait}
                  filter={this.changeFilter}
                >
                  {() => this.props.children(transformResponse(response))}
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          );
        }}
      </SyndesisFetch>
    );
  }
}
