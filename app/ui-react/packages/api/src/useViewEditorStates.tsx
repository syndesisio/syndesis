import { ViewEditorState } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewEditorStates = (virtualization: string) => {
  const url =
    'service/userProfile/viewEditorState' +
    (virtualization ? '?virtualization=' + virtualization : '');
  return useApiResource<ViewEditorState[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
