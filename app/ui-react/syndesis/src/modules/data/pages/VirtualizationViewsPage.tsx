import {
  useViewDefinitionDescriptors,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { QueryResults, ViewDefinitionDescriptor } from '@syndesis/models';
import { RestDataService } from '@syndesis/models';
import {
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
  virtualization: RestDataService;
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
  const [description, setDescription] = React.useState(
    state.virtualization.tko__description
  );
  const {
    deleteViewDefinition,
    updateVirtualizationDescription,
  } = useVirtualizationHelpers();
  const {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishServiceVdb,
  } = VirtualizationHandlers();

  const queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };

  const filterUndefinedId = (view: ViewDefinitionDescriptor): boolean => {
    return view.name !== undefined;
  };

  const {
    resource: viewDefinitionDescriptors,
    hasData: hasViewDefinitionDescriptors,
    error: viewDefinitionDescriptorsError,
    read,
  } = useViewDefinitionDescriptors(params.virtualizationId);

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
    await handlePublishVirtualization(pVirtualizationId, hasViews);
  };

  const doUnpublish = async (serviceVdbName: string) => {
    await handleUnpublishServiceVdb(serviceVdbName);
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

  const handleDeleteView = async (viewId: string, viewName: string) => {
    // Delete the view
    try {
      await deleteViewDefinition(viewId);

      pushNotification(
        t('virtualization.deleteViewSuccess', {
          name: viewName,
        }),
        'success'
      );

      await read();
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.deleteViewFailed', {
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
                /* TD-636: Commented out for TP
                        i18nExport={t('shared:Export')}
                */
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
                    onExport={
                    this.handleExportVirtualization
              } */
                onUnpublish={doUnpublish}
                onPublish={doPublish}
                serviceVdbName={state.virtualization.serviceVdbName}
                hasViews={viewDefinitionDescriptors.length > 0}
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
                i18nUnpublishInProgress={t(
                  'virtualization.unpublishInProgress'
                )}
                i18nPublishLogUrlText={t('shared:viewLogs')}
                odataUrl={getOdataUrl(state.virtualization)}
                publishedState={publishingDetails.state}
                publishingCurrentStep={publishingDetails.stepNumber}
                publishingLogUrl={publishingDetails.logUrl}
                publishingTotalSteps={publishingDetails.stepTotal}
                publishingStepText={publishingDetails.stepText}
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
                error={viewDefinitionDescriptorsError !== false}
                loading={!hasViewDefinitionDescriptors}
                loaderChildren={
                  <ViewListSkeleton
                    width={800}
                    style={{
                      backgroundColor: '#FFF',
                      marginTop: 30,
                    }}
                  />
                }
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
                    i18nDescription={t(
                      'data:virtualization.viewsPageDescription'
                    )}
                    i18nEmptyStateInfo={t(
                      'data:virtualization.viewEmptyStateInfo'
                    )}
                    i18nEmptyStateTitle={t(
                      'data:virtualization.viewEmptyStateTitle'
                    )}
                    i18nImportViews={t('data:virtualization.importDataSource')}
                    i18nImportViewsTip={t(
                      'data:virtualization.importDataSourceTip'
                    )}
                    i18nCreateView={t('data:virtualization.createView')}
                    i18nCreateViewTip={t('data:virtualization.createViewTip')}
                    i18nName={t('shared:Name')}
                    i18nNameFilterPlaceholder={t(
                      'shared:nameFilterPlaceholder'
                    )}
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    linkCreateViewHRef={resolvers.data.virtualizations.views.createView.selectSources(
                      {
                        virtualization: state.virtualization,
                      }
                    )}
                    linkImportViewsHRef={resolvers.data.virtualizations.views.importSource.selectConnection(
                      {
                        virtualization: state.virtualization,
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
                                virtualization: state.virtualization,
                                // tslint:disable-next-line: object-literal-sort-keys
                                viewDefinitionId: viewDefinitionDescriptor.id,
                                viewDefinition: undefined,
                                previewExpanded:true,
                                queryResults:queryResultsEmpty
                              }
                            )}
                            i18nCancelText={t('shared:Cancel')}
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteModalMessage={t(
                              'virtualization.deleteViewModalMessage',
                              {
                                name: viewDefinitionDescriptor.name,
                              }
                            )}
                            i18nDeleteModalTitle={t(
                              'virtualization.deleteModalTitle'
                            )}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('view.editViewTip')}
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
