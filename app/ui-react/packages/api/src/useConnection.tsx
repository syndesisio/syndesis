import { ConnectionOverview } from '@syndesis/models';
import {
  getActionsWithFrom,
  getActionsWithTo,
  isConfigurationRequired,
  isDerived,
  isTechPreview,
} from './helpers';
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
    configRequired: isConfigurationRequired(connection),
    derived: isDerived(connection),
    isTechPreview: isTechPreview(connection.connector!),
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
