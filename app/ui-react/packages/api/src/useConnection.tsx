import { IConnectionOverview } from '@syndesis/models';
import {
  getActionsWithFrom,
  getActionsWithTo,
  isConfigurationRequired,
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
    configRequired: isConfigurationRequired(connection),
    connector,
    derived: isDerived(connection),
    isTechPreview: connector ? connector.isTechPreview : false,
  };
};

export const useConnection = (
  connectionId: string,
  initialValue?: IConnectionOverview
) => {
  return useApiResource<IConnectionOverview>({
    defaultValue: {
      configRequired: false,
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
