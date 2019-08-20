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
      name: '',
      sourcePaths: [],
    },
    initialValue: initialDefn,
    url: `service/userProfile/viewEditorState/${viewDefinitionId}`,
    useDvApiUrl: true,
  });
};
