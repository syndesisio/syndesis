import { SchemaNode } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useVirtualizationConnectionSchema = (connectionId?: string) => {
  const url = connectionId
    ? `metadata/${connectionId}/schema`
    : `metadata/connection-schema`;

  return useApiResource<SchemaNode[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
