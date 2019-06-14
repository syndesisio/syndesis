import { IConnectionOverview } from '@syndesis/models';
import {
  getActionsWithFrom,
  getActionsWithTo,
  isConfigRequired,
  isDerived,
} from './helpers';
import { useApiResource } from './useApiResource';
import { transformConnectorResponse } from './useConnector';

export const transformConnectionResponse = (
  connection: IConnectionOverview
) => {
  const connector = connection.connector
    ? transformConnectorResponse(connection.connector)
    : undefined;
  return {
    ...connection,
    actionsWithFrom: getActionsWithFrom(connector ? connector.actions : []),
    actionsWithTo: getActionsWithTo(connector ? connector.actions : []),
    connector,
    derived: isDerived(connection),
    isConfigRequired: isConfigRequired(connection),
    isTechPreview: connector ? connector.isTechPreview : false,
  };
};

export const useConnection = (
  connectionId: string,
  initialValue?: IConnectionOverview
) => {
  return useApiResource<IConnectionOverview>({
    defaultValue: {
      isConfigRequired: false,
      isTechPreview: false,
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
