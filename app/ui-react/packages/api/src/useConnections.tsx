import { IConnectionOverview } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { transformConnectionResponse } from './useConnection';
import { useServerEvents } from './useServerEvents';
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

export const useConnections = (disableUpdates: boolean = false) => {
  const { read, ...rest } = useApiResource<IConnectionsResponse>({
    defaultValue: {
      connectionsForDisplay: [],
      connectionsWithFromAction: [],
      connectionsWithToAction: [],
      dangerouslyUnfilteredConnections: [],
      totalCount: 0,
    },
    transformResponse: async response => {
      const data = (await response.json()) as IConnectionsFetchResponse;
      const connections = data.items.map(transformConnectionResponse);
      return {
        connectionsForDisplay: getConnectionsForDisplay(connections),
        connectionsWithFromAction: getConnectionsWithFromAction(connections),
        connectionsWithToAction: getConnectionsWithToAction(connections),
        dangerouslyUnfilteredConnections: connections,
        totalCount: data.totalCount,
      };
    },
    url: `/connections?per_page=50`,
  });

  useServerEvents({
    disableUpdates,
    filter: (change: IChangeEvent) => change.kind.startsWith('connection'),
    read,
  });

  return {
    ...rest,
    read,
  };
};
