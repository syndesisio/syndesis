import { VirtualizationSourceStatus } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useVirtualizationConnectionStatuses = () => {
  return useApiResource<VirtualizationSourceStatus[]>({
    defaultValue: [],
    url: 'metadata/syndesisSourceStatuses',
    useDvApiUrl: true,
  });
};
