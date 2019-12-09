import { VirtualizationMetrics } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

/**
 * @param virtualizationName the name of the virtualization whose metrics are being requested
 * @param disableUpdates `true` if polling should be enabled (defaults to `false`)
 */
export const useVirtualizationMetrics = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<VirtualizationMetrics>({
    defaultValue: {
      requestCount: 0,
      resultSetCacheHitRatio: 0,
      sessions: 0,
      startedAt: '',
    },
    url: `virtualizations/${virtualizationName}/metrics`,
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
