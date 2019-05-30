import { WithViewEditorStates, WithVirtualizationHelpers } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import { ViewDefinition, ViewEditorState } from '@syndesis/models';
import { Breadcrumb, PageSection, ViewHeader } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { VirtualizationNavBar } from '../shared';

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
  WithRouteData,
} from '@syndesis/utils';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
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

export class VirtualizationViewsPage extends React.Component<
  IVirtualizationViewsPageRouteParams,
  IVirtualizationViewsPageRouteState
> {
  public filterUndefinedId(view: ViewDefinition): boolean {
    return view.viewName !== undefined;
  }

  public handleImportView(viewName: string) {
    // TODO: implement handleImportView
    alert('Import view ' + viewName);
  }

  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => {
              return (
                <WithRouteData<
                  IVirtualizationViewsPageRouteParams,
                  IVirtualizationViewsPageRouteState
                >>
                  {({ virtualizationId }, { virtualization }, { history }) => {
                    return (
                      <>
                        <Breadcrumb>
                          <Link
                            data-testid={'virtualization-views-page-home-link'}
                            to={resolvers.dashboard.root()}
                          >
                            {t('shared:Home')}
                          </Link>
                          <Link
                            data-testid={
                              'virtualization-views-page-virtualizations-link'
                            }
                            to={resolvers.data.root()}
                          >
                            {t('shared:DataVirtualizations')}
                          </Link>
                          <span>
                            {virtualizationId + ' '}
                            {t('data:virtualization.views')}
                          </span>
                        </Breadcrumb>
                        <ViewHeader
                          i18nTitle={virtualization.keng__id}
                          i18nDescription={virtualization.tko__description}
                        />
                        <WithViewEditorStates
                          idPattern={virtualization.serviceVdbName + '*'}
                        >
                          {({ data, hasData, error, read }) => {
                            return (
                              <WithVirtualizationHelpers>
                                {({ deleteView }) => {
                                  const handleDeleteView = async (
                                    viewName: string
                                  ) => {
                                    try {
                                      await deleteView(
                                        virtualization,
                                        viewName
                                      ).then(read);
                                      pushNotification(
                                        t('virtualization.deleteViewSuccess', {
                                          name: viewName,
                                        }),
                                        'success'
                                      );
                                    } catch (error) {
                                      const details = error.message
                                        ? error.message
                                        : '';
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
                                        const viewDefns = data.map(
                                          (editorState: ViewEditorState) =>
                                            editorState.viewDefinition
                                        );
                                        const filteredAndSorted = getFilteredAndSortedViewDefns(
                                          viewDefns,
                                          helpers.activeFilters,
                                          helpers.currentSortType,
                                          helpers.isSortAscending
                                        );
                                        return (
                                          <WithLoader
                                            error={error}
                                            loading={!hasData}
                                            loaderChildren={
                                              <ViewListSkeleton
                                                width={800}
                                                style={{
                                                  backgroundColor: '#FFF',
                                                  marginTop: 30,
                                                }}
                                              />
                                            }
                                            errorChildren={<ApiError />}
                                          >
                                            {() => (
                                              <>
                                                <PageSection
                                                  variant={'light'}
                                                  noPadding={true}
                                                >
                                                  <VirtualizationNavBar
                                                    virtualization={
                                                      virtualization
                                                    }
                                                  />
                                                </PageSection>
                                                <ViewList
                                                  filterTypes={filterTypes}
                                                  sortTypes={sortTypes}
                                                  {...this.state}
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
                                                  i18nName={t('shared:Name')}
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
                                                  onImportView={
                                                    this.handleImportView
                                                  }
                                                  hasListData={data.length > 0}
                                                >
                                                  {filteredAndSorted
                                                    .filter(
                                                      (
                                                        viewDefinition: ViewDefinition
                                                      ) =>
                                                        this.filterUndefinedId(
                                                          viewDefinition
                                                        )
                                                    )
                                                    .map(
                                                      (
                                                        viewDefinition: ViewDefinition,
                                                        index: number
                                                      ) => (
                                                        <ViewListItem
                                                          key={index}
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
                                        );
                                      }}
                                    </WithListViewToolbarHelpers>
                                  );
                                }}
                              </WithVirtualizationHelpers>
                            );
                          }}
                        </WithViewEditorStates>
                      </>
                    );
                  }}
                </WithRouteData>
              );
            }}
          </UIContext.Consumer>
        )}
      </Translation>
    );
  }
}
