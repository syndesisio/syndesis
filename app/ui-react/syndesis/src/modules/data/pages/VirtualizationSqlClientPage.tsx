import {
  usePolling,
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { RestDataService, 
         VirtualizationPublishingDetails
} from '@syndesis/models';
import {
  PageSection,
  SqlClientContentSkeleton,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import {
  getOdataUrl,
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
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );
  const [description, setDescription] = React.useState(
    state.virtualization.tko__description
  );
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const { updateVirtualizationDescription } = useVirtualizationHelpers();
  const {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishVirtualization,
  } = VirtualizationHandlers();
  const [publishedState, setPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [usedBy, setUsedBy] = React.useState( state.virtualization.usedBy );

  const {
    resource: viewDefinitionDescriptors,
    error,
    loading,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  const updatePublishedState = async () => {
    const publishedDetails: VirtualizationPublishingDetails = getPublishingDetails(
      appContext.config.consoleUrl,
      virtualization
    ) as VirtualizationPublishingDetails;

    setPublishedState(publishedDetails);
    setUsedBy(virtualization.usedBy);
  };

  // poll to check for updates to the published state
  usePolling({ callback: updatePublishedState, delay: 5000 });

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };
  
  const doDelete = async (pVirtualizationId: string) => {
    const success = await handleDeleteVirtualization(pVirtualizationId);
    if (success) {
      history.push(resolvers.data.virtualizations.list());
    }
  };

  const doPublish = async (pVirtualizationId: string, hasViews: boolean) => {
    await handlePublishVirtualization(pVirtualizationId, hasViews);
  };

  const doUnpublish = async (virtualizationName: string) => {
    await handleUnpublishVirtualization(virtualizationName);
  };


  const doSetDescription = async (newDescription: string) => {
    const previous = description;
    setDescription(newDescription); // this sets InlineTextEdit component to new value
    try {
      await updateVirtualizationDescription(
        appContext.user.username || 'developer',
        params.virtualizationId,
        newDescription
      );
      state.virtualization.tko__description = newDescription;
      return true;
    } catch {
      pushNotification(
        t('virtualization.errorUpdatingDescription', {
          name: state.virtualization.keng__id,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
  };

  return (
    <>
      <PageSection variant={'light'} noPadding={true}>
        <ViewHeaderBreadcrumb
          currentPublishedState={publishedState.state}
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
          hasViews={!state.virtualization.empty}
          usedInIntegration={usedBy.length > 0}
        />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationDetailsHeader
          i18nDescriptionPlaceholder={t(
            'virtualization.descriptionPlaceholder'
          )}
          i18nDraft={t('shared:Draft')}
          i18nError={t('shared:Error')}
          i18nInUseText={getUsedByMessage(usedBy)}
          i18nPublished={t('virtualization.publishedDataVirtualization')}
          i18nPublishInProgress={t('virtualization.publishInProgress')}
          i18nUnpublishInProgress={t('virtualization.unpublishInProgress')}
          i18nPublishLogUrlText={t('shared:viewLogs')}
          odataUrl={getOdataUrl(virtualization)}
          publishedState={publishedState.state || 'Loading'}
          publishingCurrentStep={publishedState.stepNumber}
          publishingLogUrl={publishedState.logUrl}
          publishingTotalSteps={publishedState.stepTotal}
          publishingStepText={publishedState.stepText}
          virtualizationDescription={description}
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
              views={viewDefinitionDescriptors}
              virtualizationId={params.virtualizationId}
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
