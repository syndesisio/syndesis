import { ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export function getConnectionsForDisplay(connections: ConnectionOverview[]) {
  return connections.filter(
    c => !c.metadata || !c.metadata['hide-from-connection-pages']
  );
}

export function getConnectionsWithFromAction(
  connections: ConnectionOverview[]
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

export function getConnectionsWithToAction(connections: ConnectionOverview[]) {
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

export interface IConnectionsResponse {
  readonly connectionsForDisplay: ConnectionOverview[];
  readonly connectionsWithToAction: ConnectionOverview[];
  readonly connectionsWithFromAction: ConnectionOverview[];
  readonly items: ConnectionOverview[];
  readonly totalCount: number;
}

export interface IWithConnectionsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IConnectionsResponse>): any;
}

export class WithConnections extends React.Component<IWithConnectionsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connection');
  }

  public render() {
    return (
      <SyndesisRest<IConnectionsResponse>
        url={'/connections'}
        defaultValue={{
          connectionsForDisplay: [],
          connectionsWithFromAction: [],
          connectionsWithToAction: [],
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(response);
          }
          return (
            <ServerEventsContext.Consumer>
              {({ registerChangeListener, unregisterChangeListener }) => (
                <WithChangeListener
                  read={read}
                  registerChangeListener={registerChangeListener}
                  unregisterChangeListener={unregisterChangeListener}
                  filter={this.changeFilter}
                >
                  {() =>
                    this.props.children({
                      ...response,
                      data: {
                        ...response.data,
                        connectionsForDisplay: getConnectionsForDisplay(
                          response.data.items
                        ),
                        connectionsWithFromAction: getConnectionsWithFromAction(
                          response.data.items
                        ),
                        connectionsWithToAction: getConnectionsWithToAction(
                          response.data.items
                        ),
                      },
                    })
                  }
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          );
        }}
      </SyndesisRest>
    );
  }
}
