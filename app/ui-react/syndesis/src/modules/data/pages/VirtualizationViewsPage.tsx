import {
  usePolling,
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import {
  ViewDefinitionDescriptor,
  VirtualizationPublishingDetails,
} from '@syndesis/models';
import { Virtualization } from '@syndesis/models';
import {
  Loader,
  PageSection,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { VirtualizationNavBar } from '../shared';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import {
  getOdataUrl,
  getPublishingDetails,
} from '../shared/VirtualizationUtils';

import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewList,
  ViewListItem,
  ViewListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';
import './VirtualizationViewsPage.css';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationViewsPageRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationViewsPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationViewsPageRouteState {
  virtualization: Virtualization;
}

function getFilteredAndSortedViewDefns(
  viewDefinitionDescriptors: ViewDefinitionDescriptor[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSorted = viewDefinitionDescriptors;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter(
      (view: ViewDefinitionDescriptor) =>
        view.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisView, thatView) => {
    if (isSortAscending) {
      return thisView.name.localeCompare(thatView.name);
    }

    // sort descending
    return thatView.name.localeCompare(thisView.name);
  });

  return filteredAndSorted;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: i18n.t('shared:filterByNamePlaceholder'),
  title: i18n.t('shared:Name'),
} as IFilterType;

const filterTypes: IFilterType[] = [filterByName];

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;

const sortTypes: ISortType[] = [sortByName];

export const VirtualizationViewsPage: React.FunctionComponent = () => {
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<
    IVirtualizationViewsPageRouteParams,
    IVirtualizationViewsPageRouteState
  >();
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );
  const [description, setDescription] = React.useState(
    state.virtualization.tko__description
  );
  const [publishedState, setPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [usedBy, setUsedBy] = React.useState(state.virtualization.usedBy);
  const {
    deleteViewDefinition,
    updateVirtualizationDescription,
  } = useVirtualizationHelpers();
  const {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishVirtualization,
  } = VirtualizationHandlers();

  const filterUndefinedId = (view: ViewDefinitionDescriptor): boolean => {
    return view.name !== undefined;
  };

  const {
    resource: viewDefinitionDescriptors,
    hasData: hasViewDefinitionDescriptors,
    error: viewDefinitionDescriptorsError,
    read,
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
      virtualization.tko__description = newDescription;
      return true;
    } catch {
      pushNotification(
        t('errorUpdatingDescription', {
          name: state.virtualization.keng__id,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
  };

  const handleDeleteView = async (viewId: string, viewName: string) => {
    // Delete the view
    try {
      await deleteViewDefinition(viewId);

      pushNotification(
        t('deleteViewSuccess', {
          name: viewName,
        }),
        'success'
      );

      await read();
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('deleteViewFailed', {
          details,
          name: viewName,
        }),
        'error'
      );
    }
  };

  return (
    <WithListViewToolbarHelpers
      defaultFilterType={filterByName}
      defaultSortType={sortByName}
    >
      {helpers => {
        const filteredAndSorted = getFilteredAndSortedViewDefns(
          viewDefinitionDescriptors,
          helpers.activeFilters,
          helpers.currentSortType,
          helpers.isSortAscending
        );
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
                i18nDeleteModalMessage={t('deleteModalMessage', {
                  name: state.virtualization.keng__id,
                })}
                i18nDeleteModalTitle={t('deleteModalTitle')}
                /* TD-636: Commented out for TP
                   i18nExport={t('shared:Export')}
                */
                i18nPublish={t('shared:Publish')}
                i18nUnpublish={t('shared:Unpublish')}
                i18nUnpublishModalMessage={t('unpublishModalMessage', {
                  name: state.virtualization.keng__id,
                })}
                i18nUnpublishModalTitle={t('unpublishModalTitle')}
                onDelete={doDelete}
                /* TD-636: Commented out for TP
                    onExport={
                    this.handleExportVirtualization
              } */
                onUnpublish={doUnpublish}
                onPublish={doPublish}
                hasViews={viewDefinitionDescriptors.length > 0}
                usedInIntegration={usedBy.length > 0}
              />
            </PageSection>
            <PageSection
              className={'virtualization-views-page'}
              variant={'light'}
              noPadding={true}
            >
              {virtualization ? (
                <VirtualizationDetailsHeader
                  i18nDescriptionPlaceholder={t('descriptionPlaceholder')}
                  i18nDraft={t('shared:Draft')}
                  i18nError={t('shared:Error')}
                  i18nInUseText={getUsedByMessage(usedBy)}
                  i18nPublished={t('publishedDataVirtualization')}
                  i18nPublishInProgress={t('publishInProgress')}
                  i18nUnpublishInProgress={t('unpublishInProgress')}
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
              ) : (
                <Loader size={'sm'} inline={true} />
              )}
            </PageSection>
            <PageSection variant={'light'} noPadding={true}>
              <VirtualizationNavBar virtualization={virtualization} />
            </PageSection>
            <PageSection variant={'light'} noPadding={true}>
              <WithLoader
                error={viewDefinitionDescriptorsError !== false}
                loading={!hasViewDefinitionDescriptors}
                loaderChildren={<ViewListSkeleton width={800} />}
                errorChildren={
                  <ApiError error={viewDefinitionDescriptorsError as Error} />
                }
              >
                {() => (
                  <ViewList
                    filterTypes={filterTypes}
                    sortTypes={sortTypes}
                    resultsCount={filteredAndSorted.length}
                    {...helpers}
                    i18nEmptyStateInfo={t('viewEmptyStateInfo')}
                    i18nEmptyStateTitle={t('viewEmptyStateTitle')}
                    i18nImportViews={t('importDataSource')}
                    i18nImportViewsTip={t('importDataSourceTip')}
                    i18nCreateView={t('createView')}
                    i18nCreateViewTip={t('createViewTip')}
                    i18nName={t('shared:Name')}
                    i18nNameFilterPlaceholder={t(
                      'shared:nameFilterPlaceholder'
                    )}
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    linkCreateViewHRef={resolvers.data.virtualizations.views.createView.selectSources(
                      {
                        virtualization,
                      }
                    )}
                    linkImportViewsHRef={resolvers.data.virtualizations.views.importSource.selectConnection(
                      {
                        virtualization,
                      }
                    )}
                    hasListData={viewDefinitionDescriptors.length > 0}
                  >
                    {filteredAndSorted
                      .filter(
                        (viewDefinitionDescriptor: ViewDefinitionDescriptor) =>
                          filterUndefinedId(viewDefinitionDescriptor)
                      )
                      .map(
                        (
                          viewDefinitionDescriptor: ViewDefinitionDescriptor,
                          index: number
                        ) => (
                          <ViewListItem
                            key={index}
                            viewId={viewDefinitionDescriptor.id}
                            viewName={viewDefinitionDescriptor.name}
                            viewDescription={
                              viewDefinitionDescriptor.description
                            }
                            viewEditPageLink={resolvers.data.virtualizations.views.edit.sql(
                              {
                                virtualization,
                                // tslint:disable-next-line: object-literal-sort-keys
                                viewDefinitionId: viewDefinitionDescriptor.id,
                                viewDefinition: undefined,
                              }
                            )}
                            i18nCancelText={t('shared:Cancel')}
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteModalMessage={t(
                              'deleteViewModalMessage',
                              {
                                name: viewDefinitionDescriptor.name,
                              }
                            )}
                            i18nDeleteModalTitle={t('deleteModalTitle')}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('shared:Edit')}
                            onDelete={handleDeleteView}
                          />
                        )
                      )}
                  </ViewList>
                )}
              </WithLoader>
            </PageSection>
          </>
        );
      }}
    </WithListViewToolbarHelpers>
  );
};
