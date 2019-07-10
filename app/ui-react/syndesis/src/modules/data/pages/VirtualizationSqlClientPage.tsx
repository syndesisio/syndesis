import { WithViewEditorStates } from '@syndesis/api';
import { RestDataService, ViewEditorState } from '@syndesis/models';
import { PageSection, ViewHeader, ViewHeaderBreadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext } from '../../../app';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import { getPreviewVdbName, getPublishingDetails } from '../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualization - the virtualization being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationSqlClientPageRouteParams#virtualizationId}.
 */
export interface IVirtualizationSqlClientPageRouteState {
  virtualization: RestDataService;
}

/**
 * Page displays virtualization views and allows user run test queries against the views.
 */
export const VirtualizationSqlClientPage: React.FunctionComponent = () => {

  const { t } = useTranslation(['data', 'shared']);
  const { state, history } = useRouteData<
    IVirtualizationSqlClientPageRouteParams,
    IVirtualizationSqlClientPageRouteState
  >();
  const appContext = React.useContext(AppContext);
  const { handleDeleteVirtualization, handlePublishVirtualization, handleUnpublishServiceVdb } = VirtualizationHandlers();
  
  const virtualization = state.virtualization;
  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    state.virtualization
  );

  const doDelete = async (
    pVirtualizationId: string
  ) => {
    const success = await handleDeleteVirtualization(pVirtualizationId);
    if(success) {
      history.push(
        resolvers.data.virtualizations.list()
      );
    }
  };

  const doPublish = async (
    pVirtualizationId: string,
    hasViews: boolean
  ) => {
    const success = await handlePublishVirtualization(pVirtualizationId,hasViews);
    if(success) {
      history.push(
        resolvers.data.virtualizations.list()
      );
    }
  }
  
  const doUnpublish = async (
    serviceVdbName: string
  ) => {
    const success = await handleUnpublishServiceVdb(serviceVdbName);
    if(success) {
      history.push(
        resolvers.data.virtualizations.list()
      );
    }
  };

  return (
    <>
      <ViewHeaderBreadcrumb
        currentPublishedState={
          publishingDetails.state
        }
        virtualizationName={
          state.virtualization.keng__id
        }
        dashboardHref={resolvers.dashboard.root()}
        dashboardString={t('shared:Home')}
        dataHref={resolvers.data.root()}
        dataString={t('shared:Virtualizations')}
        i18nViews={t('virtualization.views')}
        i18nCancelText={t('shared:Cancel')}
        i18nDelete={t('shared:Delete')}
        i18nDeleteModalMessage={t(
          'virtualization.deleteModalMessage',
          {
            name: state.virtualization.keng__id,
          }
        )}
        i18nDeleteModalTitle={t(
          'virtualization.deleteModalTitle'
        )}
        i18nPublish={t('shared:Publish')}
        i18nUnpublish={t('shared:Unpublish')}
        i18nUnpublishModalMessage={t(
          'virtualization.unpublishModalMessage',
          {
            name: state.virtualization.keng__id,
          }
        )}
        i18nUnpublishModalTitle={t(
          'virtualization.unpublishModalTitle'
        )}
        onDelete={doDelete}
        /* TD-636: Commented out for TP
           onExport={this.handleExportVirtualization} */
        onUnpublish={doUnpublish}
        onPublish={doPublish}
        serviceVdbName={
          state.virtualization.serviceVdbName
        }
        hasViews={
          state.virtualization.serviceViewDefinitions &&
          state.virtualization.serviceViewDefinitions
            .length > 0
        }
      />
      <ViewHeader
        i18nTitle={state.virtualization.keng__id}
        i18nDescription={
          state.virtualization.tko__description
        }
      />
      <PageSection
        variant={'light'}
        noPadding={true}
      >
        <VirtualizationNavBar
          virtualization={state.virtualization}
        />
      </PageSection>
      <WithViewEditorStates
        idPattern={
          state.virtualization.serviceVdbName + '*'
        }
      >
        {({ data, hasData, error }) => (
          <WithVirtualizationSqlClientForm
            views={data.map(
              (editorState: ViewEditorState) =>
                editorState.viewDefinition
            )}
            targetVdb={getPreviewVdbName()}
            linkCreateView={resolvers.data.virtualizations.create()}
            linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
              { virtualization }
            )}
          >
            {({
              form,
              submitForm,
              isSubmitting,
            }) => <></>}
          </WithVirtualizationSqlClientForm>
        )}
      </WithViewEditorStates>
    </>
  );

}
