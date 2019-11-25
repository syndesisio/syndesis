import { VirtualizationEdition } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

/**
 * @param virtualizationName the name of the virtualization whose published editions are being requested
 * @param disableUpdates `true` if polling should be enabled (defaults to `false`)
 */
export const useVirtualizationEditions = (  
  virtualizationName: string,
  disableUpdates: boolean = false) => 
{
  const { read, ...rest } = useApiResource<VirtualizationEdition[]>({
    defaultValue: [],
    url: `virtualizations/publish/${virtualizationName}`, // TODO: add in ?per_page=50?
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
