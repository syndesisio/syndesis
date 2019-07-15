import { RestDataService } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

export const useVirtualizations = (disableUpdates: boolean = false) => {
  const { read, ...rest } = useApiResource<RestDataService[]>({
    defaultValue: [],
    url: 'workspace/dataservices', // TODO: add in ?per_page=50?
    useDvApiUrl: true,
  });

  if (!disableUpdates) {
    usePolling({ polling: 5000, read });
  }

  return {
    ...rest,
    read,
  };
};
