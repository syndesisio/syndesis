import { Connector } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const transformConnectorResponse = (connector: Connector) => {
  return {
    ...connector,
  };
};

export const useConnector = (connectorId: string, initialValue?: Connector) => {
  return useApiResource<Connector>({
    defaultValue: {
      actions: [],
      name: '',
    },
    initialValue,
    transformResponse: async response => {
      const connector = await response.json();
      return transformConnectorResponse(connector);
    },
    url: `/connectors/${connectorId}`,
  });
};
