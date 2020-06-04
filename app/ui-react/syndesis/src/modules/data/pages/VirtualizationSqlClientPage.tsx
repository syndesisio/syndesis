import { useViewDefinitionDescriptors, useVirtualization } from '@syndesis/api';
import {
  SqlClientContentSkeleton 
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../shared';
import resolvers from '../../resolvers';
import { 
  VirtualizationActionId,
  WithVirtualizationSqlClientForm 
} from '../shared';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

/**
 * A page that displays virtualization views and allows user to run test queries against the views.
 */
export const VirtualizationSqlClientPage: React.FunctionComponent = () => {
  /**
   * Hook to obtain route params and history.
   */
  const { params, state } = useRouteData<
    IVirtualizationEditorPageRouteParams,
    IVirtualizationEditorPageRouteState
  >();

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);
  /**
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */

  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Hook to obtain view descriptors. Also does polling to get any view descriptor updates.
   * `loading` will be `true` each time polling occurs but the virtualization might not have changed.
   */
  const {
    error,
    hasData: hasViewDefinitionDescriptors,
    model: viewDefinitionDescriptors,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  return (
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
      items={[VirtualizationActionId.Stop, VirtualizationActionId.Delete]}
      actions={[VirtualizationActionId.Publish]}
      publishActionCustomProps={{ as: 'default' }}
    >
      <PageTitle title={t('sqlClientPageTitle')} />
      <WithLoader
        error={error !== false}
        loading={!hasViewDefinitionDescriptors}
        loaderChildren={<SqlClientContentSkeleton />}
        errorChildren={<ApiError error={error as Error} />}
      >
        {() => (
          <WithVirtualizationSqlClientForm
            views={viewDefinitionDescriptors}
            virtualizationId={params.virtualizationId}
            linkCreateView={resolvers.data.virtualizations.create()}
            linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
              { virtualization }
            )}
          >
            {() => <></>}
          </WithVirtualizationSqlClientForm>
        )}
      </WithLoader>
    </VirtualizationEditorPage>
  );
};
