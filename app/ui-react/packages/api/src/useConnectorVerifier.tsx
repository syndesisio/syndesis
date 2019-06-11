import { Result } from '@syndesis/models';
import { Connection } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useConnectorVerifier = () => {
  const { read, ...rest } = useApiResource<Result[]>({
    defaultValue: [],
    readOnMount: false,
    url: '',
  });

  return {
    ...rest,
    read: (
      connectorId: string,
      data: Pick<Connection, 'configuredProperties'>
    ) =>
      read({
        body: data,
        method: 'POST',
        url: `/connectors/${connectorId}/verifier`,
      }),
  };
};
