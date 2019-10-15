import { SchemaNode } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useVirtualizationConnectionSchema = (teiidSourceName?: string) => {
  const url = teiidSourceName
    ? `metadata/${teiidSourceName}/schema`
    : `metadata/connection-schema`;

  return useApiResource<SchemaNode[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
