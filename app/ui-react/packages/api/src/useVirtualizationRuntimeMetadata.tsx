import { ViewSourceInfo } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

/**
 * @param virtualizationName the name of the virtualization whose metadata is being requested
 * @param disableUpdates `true` if polling should be enabled (defaults to `false`)
 */
export const useVirtualizationRuntimeMetadata = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<ViewSourceInfo>({
    defaultValue: {
      schemas: [],
      viewName: ''
    },
    url: `metadata/runtimeMetadata/${virtualizationName}`,
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
