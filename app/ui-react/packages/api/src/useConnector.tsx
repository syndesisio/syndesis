import { IConnector } from '@syndesis/models';
import { isTechPreview } from './helpers';
import { useApiResource } from './useApiResource';

export const transformConnectorResponse = (connector: IConnector) => {
  return {
    ...connector,
    isTechPreview: isTechPreview(connector),
  };
};

export const useConnector = (
  connectorId: string,
  initialValue?: IConnector
) => {
  return useApiResource<IConnector>({
    defaultValue: {
      actions: [],
      isTechPreview: false,
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
