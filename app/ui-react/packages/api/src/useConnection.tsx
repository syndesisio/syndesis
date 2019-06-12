import { IConnectionOverview } from '@syndesis/models';
import {
  getActionsWithFrom,
  getActionsWithTo,
  isConfigurationRequired,
  isDerived,
  isTechPreview,
} from './helpers';
import { useApiResource } from './useApiResource';

export const transformConnectionResponse = (
  connection: IConnectionOverview
) => {
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
  initialValue?: IConnectionOverview
) => {
  return useApiResource<IConnectionOverview>({
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
