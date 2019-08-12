import { useViewEditorStates, useVirtualizationHelpers } from '@syndesis/api';
import { RestDataService, ViewEditorState } from '@syndesis/models';
import {
  PageSection,
  SqlClientContentSkeleton,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext } from '../../../app';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import {
  getOdataUrl,
  getPreviewVdbName,
  getPublishingDetails,
} from '../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
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
  const { params, state, history } = useRouteData<
    IVirtualizationSqlClientPageRouteParams,
    IVirtualizationSqlClientPageRouteState
  >();
  const appContext = React.useContext(AppContext);
  const { updateVirtualizationDescription } = useVirtualizationHelpers();
  const {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishServiceVdb,
  } = VirtualizationHandlers();

  const { resource: editorStates, error, loading } = useViewEditorStates(
    params.virtualizationId
  );

  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    state.virtualization
  );

  const doDelete = async (pVirtualizationId: string) => {
    const success = await handleDeleteVirtualization(pVirtualizationId);
    if (success) {
      history.push(resolvers.data.virtualizations.list());
    }
  };

  const doPublish = async (pVirtualizationId: string, hasViews: boolean) => {
    const success = await handlePublishVirtualization(
      pVirtualizationId,
      hasViews
    );
    if (success) {
      history.push(resolvers.data.virtualizations.list());
    }
  };

  const doUnpublish = async (serviceVdbName: string) => {
    const success = await handleUnpublishServiceVdb(serviceVdbName);
    if (success) {
      history.push(resolvers.data.virtualizations.list());
    }
  };

  const doSetDescription = async (newDescription: string) => {
    await updateVirtualizationDescription(
      appContext.user.username || 'developer',
      params.virtualizationId,
      newDescription
    );
    state.virtualization.tko__description = newDescription;
    return true;
  };

  return (
    <>
      <PageSection variant={'light'} noPadding={true}>
        <ViewHeaderBreadcrumb
          currentPublishedState={publishingDetails.state}
          virtualizationName={state.virtualization.keng__id}
          dashboardHref={resolvers.dashboard.root()}
          dashboardString={t('shared:Home')}
          dataHref={resolvers.data.root()}
          dataString={t('shared:Virtualizations')}
          i18nCancelText={t('shared:Cancel')}
          i18nDelete={t('shared:Delete')}
          i18nDeleteModalMessage={t('virtualization.deleteModalMessage', {
            name: state.virtualization.keng__id,
          })}
          i18nDeleteModalTitle={t('virtualization.deleteModalTitle')}
          i18nPublish={t('shared:Publish')}
          i18nUnpublish={t('shared:Unpublish')}
          i18nUnpublishModalMessage={t('virtualization.unpublishModalMessage', {
            name: state.virtualization.keng__id,
          })}
          i18nUnpublishModalTitle={t('virtualization.unpublishModalTitle')}
          onDelete={doDelete}
          /* TD-636: Commented out for TP
            onExport={this.handleExportVirtualization} */
          onUnpublish={doUnpublish}
          onPublish={doPublish}
          serviceVdbName={state.virtualization.serviceVdbName}
          hasViews={
            state.virtualization.serviceViewDefinitions &&
            state.virtualization.serviceViewDefinitions.length > 0
          }
        />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationDetailsHeader
          i18nDescriptionPlaceholder={t(
            'virtualization.descriptionPlaceholder'
          )}
          i18nDraft={t('shared:Draft')}
          i18nError={t('shared:Error')}
          i18nPublished={t('virtualization.publishedDataVirtualization')}
          i18nPublishInProgress={t('virtualization.publishInProgress')}
          i18nUnpublishInProgress={t('virtualization.unpublishInProgress')}
          i18nPublishLogUrlText={t('shared:viewLogs')}
          odataUrl={getOdataUrl(state.virtualization)}
          publishedState={publishingDetails.state}
          publishingCurrentStep={publishingDetails.stepNumber}
          publishingLogUrl={publishingDetails.logUrl}
          publishingTotalSteps={publishingDetails.stepTotal}
          publishingStepText={publishingDetails.stepText}
          virtualizationDescription={state.virtualization.tko__description}
          virtualizationName={state.virtualization.keng__id}
          isWorking={false}
          onChangeDescription={doSetDescription}
        />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={state.virtualization} />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <WithLoader
          error={error !== false}
          loading={loading}
          loaderChildren={<SqlClientContentSkeleton />}
          errorChildren={<ApiError error={error as Error} />}
        >
          {() => (
            <WithVirtualizationSqlClientForm
              views={editorStates.map(
                (editorState: ViewEditorState) => editorState.viewDefinition
              )}
              targetVdb={getPreviewVdbName()}
              linkCreateView={resolvers.data.virtualizations.create()}
              linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
                { virtualization: state.virtualization }
              )}
            >
              {() => <></>}
            </WithVirtualizationSqlClientForm>
          )}
        </WithLoader>
      </PageSection>
    </>
  );
};
