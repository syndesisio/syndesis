import { AcquisitionMethod } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useConnectorCredentials = (connectorId: string) => {
  return useApiResource<AcquisitionMethod | undefined>({
    defaultValue: {},
    url: `/connectors/${connectorId}/credentials`,
  });
};
