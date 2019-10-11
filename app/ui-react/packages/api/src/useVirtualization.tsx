import { Virtualization } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

export const useVirtualization = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<Virtualization>({
    defaultValue: {
      empty: true,
      id: '',
      keng__id: '',
      publishedState: 'NOTFOUND',
      serviceViewModel: '',
      tko__description: '',
      usedBy: []
    },
    url: `virtualizations/${virtualizationName}`,
    useDvApiUrl: true,
  });

  if (!disableUpdates) {
    usePolling({ callback: read, delay: 5000 });
  }

  return {
    ...rest,
    read,
  };
};
