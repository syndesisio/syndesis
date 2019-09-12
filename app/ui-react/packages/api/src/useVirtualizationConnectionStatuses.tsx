import { VirtualizationSourceStatus } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

export const useVirtualizationConnectionStatuses = (disableUpdates: boolean = false) => {
  const { read, ...rest } = useApiResource<VirtualizationSourceStatus[]>({
    defaultValue: [],
    url: 'metadata/syndesisSourceStatuses',
    useDvApiUrl: true,
  });

  if (!disableUpdates) {
    usePolling({ callback: read, delay: 10000 });
  }

  return {
    ...rest,
    read,
  };
};
