import { RestDataService } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

export const useVirtualization = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<RestDataService>({
    defaultValue: {
      connections: 0,
      drivers: 0,
      keng___links: [],
      keng__baseUri: '',
      keng__dataPath: '',
      keng__hasChildren: false,
      keng__id: '',
      keng__kType: 'Dataservice',
      publishedState: 'NOTFOUND',
      serviceVdbName: '',
      serviceVdbVersion: '1',
      serviceViewDefinitions: [],
      serviceViewModel: '',
      tko__description: '',
    },
    url: `workspace/dataservices/${virtualizationName}`,
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
