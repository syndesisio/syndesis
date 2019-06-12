import { Connector } from '@syndesis/models';
import { isTechPreview } from './helpers';
import { useApiResource } from './useApiResource';

export const transformConnectorResponse = (connector: Connector) => {
  return {
    ...connector,
    isTechPreview: isTechPreview(connector),
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
