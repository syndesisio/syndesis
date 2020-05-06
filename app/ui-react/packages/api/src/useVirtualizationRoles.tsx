import { RoleInfo } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

/**
 * @param virtualizationName the name of the virtualization whose roles are being requested
 * @param disableUpdates `true` if polling should be enabled (defaults to `false`)
 */
export const useVirtualizationRoles = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<RoleInfo>({
    defaultValue: {
      operation: 'GRANT',
      tablePrivileges: [],
    },
    url: `virtualizations/${virtualizationName}/roles`,
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
