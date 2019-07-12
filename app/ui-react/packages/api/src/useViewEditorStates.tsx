import { ViewEditorState } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useViewEditorStates = (idPattern?: string) => {
  const url =
    'service/userProfile/viewEditorState' +
    (idPattern ? '?pattern=' + idPattern : '');
  return useApiResource<ViewEditorState[]>({
    defaultValue: [],
    url,
    useDvApiUrl: true,
  });
};
