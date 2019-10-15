import { ViewDefinition } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewDefinition = (
  viewDefinitionId: string, initialDefn?: ViewDefinition
) => {
  return useApiResource<ViewDefinition>({
    defaultValue: {
      complete: false,
      dataVirtualizationName: '',
      description: '',
      message: '',
      name: '',
      sourcePaths: [],
      status: 'ERROR',
      userDefined: false,
    },
    initialValue: initialDefn,
    url: `editors/${viewDefinitionId}`,
    useDvApiUrl: true,
  });
};
