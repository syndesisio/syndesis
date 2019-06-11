import { AcquisitionMethod } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useConnectorCredentials = (connectorId: string) => {
  return useApiResource<AcquisitionMethod | undefined>({
    defaultValue: {},
    transformResponse: async response => {
      const am = await response.json();
      return Object.keys(am).length > 0 ? am : undefined;
    },
    url: `/connectors/${connectorId}/credentials`,
  });
};
