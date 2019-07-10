import { useVirtualizationHelpers, WithViewEditorStates } from '@syndesis/api';
import { ViewDefinition, ViewEditorState } from '@syndesis/models';
import { RestDataService } from '@syndesis/models';
import { PageSection, ViewHeader, ViewHeaderBreadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { VirtualizationNavBar } from '../shared';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import { getPublishingDetails } from '../shared/VirtualizationUtils';

import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewList,
  ViewListItem,
  ViewListSkeleton,
} from '@syndesis/ui';
import {
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationViewsPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationViewsPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationViewsPageRouteState {
  virtualization: RestDataService;
}

function getFilteredAndSortedViewDefns(
  viewDefinitions: ViewDefinition[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSorted = viewDefinitions;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((view: ViewDefinition) =>
      view.viewName.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisView, thatView) => {
    if (isSortAscending) {
      return thisView.viewName.localeCompare(thatView.viewName);
    }

    // sort descending
    return thatView.viewName.localeCompare(thisView.viewName);
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
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { state, history } = useRouteData<
    IVirtualizationViewsPageRouteParams,
    IVirtualizationViewsPageRouteState
  >();
  const { deleteView } = useVirtualizationHelpers();
  const { handleDeleteVirtualization, handlePublishVirtualization, handleUnpublishServiceVdb } = VirtualizationHandlers();

  const filterUndefinedId = (view: ViewDefinition): boolean => {
    return view.viewName !== undefined;
  }

  const virtualization = state.virtualization;

  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    virtualization
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
      <WithViewEditorStates
        idPattern={
          virtualization.serviceVdbName + '*'
        }
      >
        {({
          data,
          hasData,
          error,
          errorMessage,
          read,
        }) => {
          const handleDeleteView = async (
            viewName: string
          ) => {
            try {
              await deleteView(
                virtualization,
                viewName
              ).then(read);

              pushNotification(
                t(
                  'virtualization.deleteViewSuccess',
                  {
                    name: viewName,
                  }
                ),
                'success'
              );
            } catch (error) {
              const details = error.message
                ? error.message
                : '';
              pushNotification(
                t(
                  'virtualization.deleteViewFailed',
                  {
                    details,
                    name: viewName,
                  }
                ),
                'error'
              );
            }
          };
          return (
            <WithListViewToolbarHelpers
              defaultFilterType={
                filterByName
              }
              defaultSortType={sortByName}
            >
              {helpers => {
                const viewDefns = data.map(
                  (
                    editorState: ViewEditorState
                  ) =>
                    editorState.viewDefinition
                ) as ViewDefinition[];
                const filteredAndSorted = getFilteredAndSortedViewDefns(
                  viewDefns,
                  helpers.activeFilters,
                  helpers.currentSortType,
                  helpers.isSortAscending
                );
                return (
                  <PageSection
                    variant={'light'}
                    noPadding={true}
                  >
                    <WithLoader
                      error={error}
                      loading={!hasData}
                      loaderChildren={
                        <ViewListSkeleton
                          width={800}
                          style={{
                            backgroundColor:
                              '#FFF',
                            marginTop: 30,
                          }}
                        />
                      }
                      errorChildren={
                        <ApiError
                          error={
                            errorMessage!
                          }
                        />
                      }
                    >
                      {() => (
                        <>
                          <PageSection
                            variant={
                              'light'
                            }
                            noPadding={true}
                          >
                            <ViewHeaderBreadcrumb
                              currentPublishedState={
                                publishingDetails.state
                              }
                              virtualizationName={
                                virtualization.keng__id
                              }
                              dashboardHref={resolvers.dashboard.root()}
                              dashboardString={t(
                                'shared:Home'
                              )}
                              dataHref={resolvers.data.root()}
                              dataString={t(
                                'shared:Virtualizations'
                              )}
                              i18nViews={t(
                                'virtualization.views'
                              )}
                              i18nCancelText={t(
                                'shared:Cancel'
                              )}
                              i18nDelete={t(
                                'shared:Delete'
                              )}
                              i18nDeleteModalMessage={t(
                                'virtualization.deleteModalMessage',
                                {
                                  name:
                                    virtualization.keng__id,
                                }
                              )}
                              i18nDeleteModalTitle={t(
                                'virtualization.deleteModalTitle'
                              )}
                              /* TD-636: Commented out for TP
                                      i18nExport={t('shared:Export')}
                              */
                              i18nPublish={t(
                                'shared:Publish'
                              )}
                              i18nUnpublish={t(
                                'shared:Unpublish'
                              )}
                              i18nUnpublishModalMessage={t(
                                'virtualization.unpublishModalMessage',
                                {
                                  name:
                                    virtualization.keng__id,
                                }
                              )}
                              i18nUnpublishModalTitle={t(
                                'virtualization.unpublishModalTitle'
                              )}
                              onDelete={
                                doDelete
                              }
                              /* TD-636: Commented out for TP
                                 onExport={
                                 this.handleExportVirtualization
                            } */
                              onUnpublish={
                                doUnpublish
                              }
                              onPublish={
                                doPublish
                              }
                              serviceVdbName={
                                virtualization.serviceVdbName
                              }
                              hasViews={
                                viewDefns.length >
                                0
                              }
                            />
                            <ViewHeader
                              i18nTitle={
                                virtualization.keng__id
                              }
                              i18nDescription={
                                virtualization.tko__description
                              }
                            />
                            <VirtualizationNavBar
                              virtualization={
                                virtualization
                              }
                            />
                          </PageSection>
                          <ViewList
                            filterTypes={
                              filterTypes
                            }
                            sortTypes={
                              sortTypes
                            }
                            resultsCount={
                              filteredAndSorted.length
                            }
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
                            i18nImportViews={t(
                              'data:virtualization.importDataSource'
                            )}
                            i18nImportViewsTip={t(
                              'data:virtualization.importDataSourceTip'
                            )}
                            i18nCreateView={t(
                              'data:virtualization.createView'
                            )}
                            i18nCreateViewTip={t(
                              'data:virtualization.createViewTip'
                            )}
                            i18nName={t(
                              'shared:Name'
                            )}
                            i18nNameFilterPlaceholder={t(
                              'shared:nameFilterPlaceholder'
                            )}
                            i18nResultsCount={t(
                              'shared:resultsCount',
                              {
                                count:
                                  filteredAndSorted.length,
                              }
                            )}
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
                            hasListData={
                              data.length >
                              0
                            }
                          >
                            {filteredAndSorted
                              .filter(
                                (
                                  viewDefinition: ViewDefinition
                                ) =>
                                  filterUndefinedId(
                                    viewDefinition
                                  )
                              )
                              .map(
                                (
                                  viewDefinition: ViewDefinition,
                                  index: number
                                ) => (
                                    <ViewListItem
                                      key={
                                        index
                                      }
                                      viewName={
                                        viewDefinition.viewName
                                      }
                                      viewDescription={
                                        viewDefinition.keng__description
                                      }
                                      viewEditPageLink={resolvers.data.virtualizations.views.edit(
                                        {
                                          virtualization,
                                          // tslint:disable-next-line: object-literal-sort-keys
                                          viewDefinition,
                                        }
                                      )}
                                      i18nCancelText={t(
                                        'shared:Cancel'
                                      )}
                                      i18nDelete={t(
                                        'shared:Delete'
                                      )}
                                      i18nDeleteModalMessage={t(
                                        'virtualization.deleteViewModalMessage',
                                        {
                                          name:
                                            viewDefinition.viewName,
                                        }
                                      )}
                                      i18nDeleteModalTitle={t(
                                        'virtualization.deleteModalTitle'
                                      )}
                                      i18nEdit={t(
                                        'shared:Edit'
                                      )}
                                      i18nEditTip={t(
                                        'view.editViewTip'
                                      )}
                                      onDelete={
                                        handleDeleteView
                                      }
                                    />
                                  )
                              )}
                          </ViewList>
                        </>
                      )}
                    </WithLoader>
                  </PageSection>
                );
              }}
            </WithListViewToolbarHelpers>
          );
        }}
      </WithViewEditorStates>
    </>
  );
}
