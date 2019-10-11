import { ViewDefinition } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewDefinition = (
  viewDefinitionId: string, initialDefn?: ViewDefinition
) => {
  return useApiResource<ViewDefinition>({
    defaultValue: {
      dataVirtualizationName: '',
      isComplete: false,
      isUserDefined: false,
      keng__description: '',
      message: '',
      name: '',
      sourcePaths: [],
      status: 'ERROR'
    },
    initialValue: initialDefn,
    url: `editors/${viewDefinitionId}`,
    useDvApiUrl: true,
  });
};
