import { Connector } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useConnector = (connectorId: string, initialValue?: Connector) => {
  return useApiResource<Connector>({
    defaultValue: {
      actions: [],
      name: '',
    },
    initialValue,
    url: `/connectors/${connectorId}`,
  });
};
