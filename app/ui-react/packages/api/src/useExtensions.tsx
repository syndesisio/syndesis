import { Extension } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { useServerEvents } from './useServerEvents';
import { IChangeEvent } from './WithServerEvents';

export interface IExtensionsResponse {
  items: Extension[];
  totalCount: number;
}

export const useExtensions = (disableUpdates: boolean = false) => {
  const { read, ...rest } = useApiResource<IExtensionsResponse>({
    defaultValue: {
      items: [],
      totalCount: 0,
    },
    url: `/extensions?per_page=50`,
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
