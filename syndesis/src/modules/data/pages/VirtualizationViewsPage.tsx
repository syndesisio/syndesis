import { WithViews } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import { RestViewDefinition } from '@syndesis/models';
import * as React from 'react';
import i18n from '../../../i18n';
import HeaderView from '../shared/HeaderView';
import VirtualizationNavBar from '../shared/VirtualizationNavBar';

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
import resolvers from '../resolvers';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationDetailRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationDetailRouteParams#virtualizationId}.
 */

export interface IVirtualizationDetailRouteState {
  virtualization: RestDataService;
}

function getFilteredAndSortedViews(
  views: RestViewDefinition[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSorted = views;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((view: RestViewDefinition) =>
      view.keng__id.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisView, thatView) => {
    if (isSortAscending) {
      return thisView.keng__id.localeCompare(thatView.keng__id);
    }

    // sort descending
    return thatView.keng__id.localeCompare(thisView.keng__id);
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

export default class VirtualizationViewsPage extends React.Component<
  IVirtualizationDetailRouteParams,
  IVirtualizationDetailRouteState
> {
  public filterUndefinedId(view: RestViewDefinition): boolean {
    return view.keng__id !== undefined;
  }

  public handleDeleteView(viewName: string) {
    // TODO: implement handleImportVirt
    alert('Import view ' + viewName);
  }

  public handleImportView(viewName: string) {
    // TODO: implement handleImportView
    alert('Import view ' + viewName);
  }

  public handleEditView() {
    // TODO: implement handleEdit
    alert('Edit view ');
  }

  public render() {
    return (
      <WithRouteData<
        IVirtualizationDetailRouteParams,
        IVirtualizationDetailRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => {
          return (
            <div>
              <HeaderView virtualizationId={virtualizationId} />
              <WithViews virtualizationId={virtualizationId}>
                {({ data, hasData, error }) => (
                  <WithListViewToolbarHelpers
                    defaultFilterType={filterByName}
                    defaultSortType={sortByName}
                  >
                    {helpers => {
                      const filteredAndSorted = getFilteredAndSortedViews(
                        data,
                        helpers.activeFilters,
                        helpers.currentSortType,
                        helpers.isSortAscending
                      );
                      return (
                        <Translation ns={['data', 'shared']}>
                          {t => (
                            <>
                              <VirtualizationNavBar
                                virtualization={virtualization}
                                virtualizationId={virtualizationId}
                              />
                              <ViewList
                                filterTypes={filterTypes}
                                sortTypes={sortTypes}
                                {...this.state}
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
                                i18nImportView={t('shared:Import')}
                                i18nImportViewTip={t(
                                  'data:virtualization.importViewTip'
                                )}
                                i18nCreateView={t('shared:Create')}
                                i18nCreateViewTip={t(
                                  'data:virtualization.createViewTip'
                                )}
                                i18nName={t('shared:Name')}
                                i18nNameFilterPlaceholder={t(
                                  'shared:nameFilterPlaceholder'
                                )}
                                i18nResultsCount={t('shared:resultsCount', {
                                  count: filteredAndSorted.length,
                                })}
                                // TODO - Point to views.create when available
                                linkCreateHRef={resolvers.virtualizations.create()}
                                onImportView={this.handleImportView}
                                hasListData={data.length > 0}
                              />
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
                                errorChildren={<div>TODO</div>}
                              >
                                {() =>
                                  filteredAndSorted
                                    .filter((view: RestViewDefinition) =>
                                      this.filterUndefinedId(view)
                                    )
                                    .map(
                                      (
                                        view: RestViewDefinition,
                                        index: number
                                      ) => (
                                        <ViewListItem
                                          key={index}
                                          viewName={view.keng__id}
                                          viewDescription={
                                            'Description goes here'
                                          }
                                          i18nDelete={t('shared:Delete')}
                                          i18nEdit={t('shared:Edit')}
                                          i18nEditTip={t('view.editViewTip')}
                                          onDelete={this.handleDeleteView}
                                          onEdit={this.handleEditView}
                                        />
                                      )
                                    )
                                }
                              </WithLoader>
                            </>
                          )}
                        </Translation>
                      );
                    }}
                  </WithListViewToolbarHelpers>
                )}
              </WithViews>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
