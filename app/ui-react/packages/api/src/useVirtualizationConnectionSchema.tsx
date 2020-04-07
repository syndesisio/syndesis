import { SchemaNode } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useVirtualizationConnectionSchema = (teiidSourceName?: string) => {
  const url = teiidSourceName
    ? `metadata/sourceSchema/${teiidSourceName}`
    : `metadata/sourceSchema`;

  return useApiResource<SchemaNode[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
