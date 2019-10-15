import { SchemaNode } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useVirtualizationConnectionSchema = (teiidSourceName?: string) => {
  const url = teiidSourceName
    ? `metadata/${teiidSourceName}/schema`
    : `metadata/connectionSchema`;

  return useApiResource<SchemaNode[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
