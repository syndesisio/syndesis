import { WithVirtualizationConnectionSchema } from '@syndesis/api';
import { SchemaNode, ViewInfo } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewInfoList,
  ViewInfoListItem,
  ViewInfoListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { generateViewInfos } from './VirtualizationUtils';

function getFilteredAndSortedViewInfos(
  schemaNodes: SchemaNode[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean,
  selectedViewNames: string[],
  existingViewNames: string[]
) {
  const viewInfos: ViewInfo[] = [];
  if (schemaNodes && schemaNodes.length > 0) {
    generateViewInfos(
      viewInfos,
      schemaNodes[0],
      [],
      selectedViewNames,
      existingViewNames
    );
  }

  let filteredAndSorted = viewInfos;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((viewInfo: ViewInfo) =>
      viewInfo.viewName.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisViewInfo, thatViewInfo) => {
    if (isSortAscending) {
      return thisViewInfo.viewName.localeCompare(thatViewInfo.viewName);
    }

    // sort descending
    return thatViewInfo.viewName.localeCompare(thisViewInfo.viewName);
  });

  return filteredAndSorted;
}

export interface IViewInfosContentProps {
  connectionName: string;
  existingViewNames: string[];
  onViewSelected: (view: ViewInfo) => void;
  onViewDeselected: (viewName: string) => void;
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

export class ViewInfosContent extends React.Component<IViewInfosContentProps> {
  public displayedViews: ViewInfo[] = [];
  public selectedViewNames: string[] = [];

  public constructor(props: IViewInfosContentProps) {
    super(props);
    this.handleViewSelectionChange = this.handleViewSelectionChange.bind(this);
  }

  public handleViewSelectionChange(name: string, selected: boolean) {
    if (selected) {
      for (const viewInfo of this.displayedViews) {
        if (viewInfo.viewName === name) {
          this.props.onViewSelected(viewInfo);
        }
      }
    } else {
      this.props.onViewDeselected(name);
    }
  }

  public render() {
    return (
      <WithVirtualizationConnectionSchema
        connectionId={this.props.connectionName}
      >
        {({ data, hasData, error }) => (
          <WithListViewToolbarHelpers
            defaultFilterType={filterByName}
            defaultSortType={sortByName}
          >
            {helpers => {
              const filteredAndSorted = getFilteredAndSortedViewInfos(
                data,
                helpers.activeFilters,
                helpers.currentSortType,
                helpers.isSortAscending,
                this.selectedViewNames,
                this.props.existingViewNames
              );
              this.displayedViews = filteredAndSorted;

              return (
                <Translation ns={['data', 'shared']}>
                  {t => (
                    <ViewInfoList
                      filterTypes={filterTypes}
                      sortTypes={sortTypes}
                      {...this.state}
                      resultsCount={filteredAndSorted.length}
                      {...helpers}
                      i18nEmptyStateInfo={t(
                        'virtualization.emptyStateInfoMessage'
                      )}
                      i18nEmptyStateTitle={t('virtualization.emptyStateTitle')}
                      i18nName={t('shared:Name')}
                      i18nNameFilterPlaceholder={t(
                        'shared:nameFilterPlaceholder'
                      )}
                      i18nResultsCount={t('shared:resultsCount', {
                        count: filteredAndSorted.length,
                      })}
                    >
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={
                          <ViewInfoListSkeleton
                            width={800}
                            style={{
                              backgroundColor: '#FFF',
                              marginTop: 30,
                            }}
                          />
                        }
                        errorChildren={<ApiError />}
                      >
                        {() =>
                          filteredAndSorted.map(
                            (viewInfo: ViewInfo, index: number) => (
                              <ViewInfoListItem
                                key={index}
                                connectionName={viewInfo.connectionName}
                                name={viewInfo.viewName}
                                nodePath={viewInfo.nodePath}
                                selected={viewInfo.selected}
                                i18nUpdate={t('shared:Update')}
                                isUpdateView={viewInfo.isUpdate}
                                onSelectionChanged={
                                  this.handleViewSelectionChange
                                }
                              />
                            )
                          )
                        }
                      </WithLoader>
                    </ViewInfoList>
                  )}
                </Translation>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </WithVirtualizationConnectionSchema>
    );
  }
}
