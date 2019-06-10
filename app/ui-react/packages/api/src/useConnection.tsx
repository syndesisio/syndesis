import { ConnectionOverview } from '@syndesis/models';
import { getActionsWithFrom, getActionsWithTo, isDerived } from './helpers';
import { useApiResource } from './useApiResource';

export const transformConnectionResponse = (connection: ConnectionOverview) => {
  return {
    ...connection,
    actionsWithFrom: getActionsWithFrom(
      connection.connector ? connection.connector.actions : []
    ),
    actionsWithTo: getActionsWithTo(
      connection.connector ? connection.connector.actions : []
    ),
    derived: isDerived(connection),
  };
};

export const useConnection = (
  connectionId: string,
  initialValue?: ConnectionOverview
) => {
  return useApiResource<ConnectionOverview>({
    defaultValue: {
      name: '',
    },
    initialValue,
    transformResponse: async response => {
      const connection = await response.json();
      return transformConnectionResponse(connection);
    },
    url: `/connections/${connectionId}`,
  });
};
