import { IntegrationOverview } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { useServerEvents } from './useServerEvents';
import { IChangeEvent } from './WithServerEvents';

export const useExtensionIntegrations = (
  extensionId: string,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<IntegrationOverview[]>({
    defaultValue: [],
    url: `/extensions/${extensionId}/integrations`,
  });

  useServerEvents({
    disableUpdates,
    filter: (change: IChangeEvent) =>
      change.kind === 'integration' || change.kind === 'integration-deployment',
    read,
  });

  return {
    ...rest,
    read,
  };
};
