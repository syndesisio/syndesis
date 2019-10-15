import { ViewDefinitionDescriptor } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewDefinitionDescriptors = (virtualizationName: string) => {
  const url = `virtualizations/${virtualizationName}/views`;
  return useApiResource<ViewDefinitionDescriptor[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
