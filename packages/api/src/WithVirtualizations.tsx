import { ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export function getVirtualizationsForDisplay(
  connections: ConnectionOverview[]
) {
  return connections.filter(
    c => !c.metadata || !c.metadata['hide-from-connection-pages']
  );
}

export function getVirtualizationsWithFromAction(
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

export function getVirtualizationsWithToAction(
  connections: ConnectionOverview[]
) {
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

export interface IVirtualizationsResponse {
  readonly connectionsForDisplay: ConnectionOverview[];
  readonly connectionsWithToAction: ConnectionOverview[];
  readonly connectionsWithFromAction: ConnectionOverview[];
  readonly items: ConnectionOverview[];
  readonly totalCount: number;
}

export interface IWithVirtualizationsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IVirtualizationsResponse>): any;
}

export class WithVirtualizations extends React.Component<
  IWithVirtualizationsProps
> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connection');
  }

  public render() {
    return (
      <SyndesisFetch<IVirtualizationsResponse>
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
                        connectionsForDisplay: getVirtualizationsForDisplay(
                          response.data.items
                        ),
                        connectionsWithFromAction: getVirtualizationsWithFromAction(
                          response.data.items
                        ),
                        connectionsWithToAction: getVirtualizationsWithToAction(
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
      </SyndesisFetch>
    );
  }
}
