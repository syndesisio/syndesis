import { ViewDefinitionDescriptor } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewDefinitionDescriptors = (virtualizationName: string) => {
  const url =
    'service/userProfile/viewListings?virtualization=' + virtualizationName;
  return useApiResource<ViewDefinitionDescriptor[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
