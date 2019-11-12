import { useVirtualization } from '@syndesis/api';
import { PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../resolvers';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

/**
 * A page that displays virtualization publish state and history.
 */
export const VirtualizationDetailsPage: React.FunctionComponent = () => {
  /**
   * Hook to obtain route params and history.
   */
  const { params, state, history } = useRouteData<
    IVirtualizationEditorPageRouteParams,
    IVirtualizationEditorPageRouteState
  >();

  /**
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Callback for when a virtualization is successfully deleted. This will route to the virtualization
   * list page.
   */
  const deleteCallback = React.useCallback(() => {
    history.push(resolvers.data.virtualizations.list().pathname);
  }, [history]);

  return (
    <VirtualizationEditorPage
      onDeleteSuccess={deleteCallback}
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
    >
      <PageSection>
        <React.Fragment>Details display coming soon...</React.Fragment>
      </PageSection>
    </VirtualizationEditorPage>
  );
};
