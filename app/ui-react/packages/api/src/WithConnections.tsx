import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { transformConnectionResponse } from './useConnection';
import {
  getConnectionsForDisplay,
  getConnectionsWithFromAction,
  getConnectionsWithToAction,
  IConnectionsFetchResponse,
  IConnectionsResponse,
} from './useConnections';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

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
