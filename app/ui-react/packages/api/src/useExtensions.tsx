import { Extension } from '@syndesis/models';
import { useApiResource } from './useApiResource';
import { useServerEvents } from './useServerEvents';
import { IChangeEvent } from './WithServerEvents';

export interface IExtensionsResponse {
  items: Extension[];
  totalCount: number;
}

type IExtensionType = 'Steps' | 'Connectors' | 'Libraries';

export const useExtensions = (
  disableUpdates: boolean = false,
  extensionType?: IExtensionType
) => {
  /**
   * Here we are allowing calls to `useExtensions` to pass an optional `extensionType`
   * query param of type IExtensionType, which gets appended to the string URL when
   * provided.
   *
   * We currently use this for listing available Integration Extensions in the final
   * step of the Integration Editor.
   */
  let extensionUrl: string = '/extensions?per_page=50';
  if (extensionType) {
    extensionUrl = `${extensionUrl}&extensionType=${extensionType}`;
  }

  const { read, ...rest } = useApiResource<IExtensionsResponse>({
    defaultValue: {
      items: [],
      totalCount: 0,
    },
    url: extensionUrl,
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
