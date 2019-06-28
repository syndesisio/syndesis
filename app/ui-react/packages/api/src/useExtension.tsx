import { Extension } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { useServerEvents } from './useServerEvents';
import { IChangeEvent } from './WithServerEvents';

export const useExtension = (
  extensionId: string,
  initialValue?: Extension,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<Extension>({
    defaultValue: {
      actions: [],
      extensionType: 'Steps',
      name: '',
      schemaVersion: '',
    },
    initialValue,
    url: `/extensions/${extensionId}`,
  });

  useServerEvents({
    disableUpdates,
    filter: (change: IChangeEvent) => change.kind.startsWith('extension'),
    read,
  });

  return {
    ...rest,
    read,
  };
};
