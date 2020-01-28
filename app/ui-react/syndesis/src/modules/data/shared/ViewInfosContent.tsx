import {
  useVirtualizationConnectionSchema,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { SchemaNode, ViewInfo } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewInfoList,
  ViewInfoListItems,
  ViewInfoListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { generateAllViewInfos } from './VirtualizationUtils';

const getSelectedViewName = (selectedViews: ViewInfo[]): string[] => {
  return selectedViews.map(view => view.viewName);
};

export interface IViewInfosContentProps {
  connectionName: string;
  existingViewNames: string[];
  onViewSelected: (view: ViewInfo) => void;
  onViewDeselected: (viewName: string) => void;
  selectedViews: ViewInfo[];
  handleSelectAll: (isSelected: boolean, AllViewInfo: any[]) => void;
  clearSelectedViews: () => void;
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

export const ViewInfosContent: React.FunctionComponent<IViewInfosContentProps> = (props) => {

  const getFilteredAndSortedViewInfos = (
    activeFilters: IActiveFilter[],
    isSortAscending: boolean,
    selectedViewsNames: string[],
    existingViewNames: string[]
  ) => {
    const viewInfos: any[] = [];
    if (schemaNodes && schemaNodes.length > 0) {
      generateAllViewInfos(
        viewInfos,
        schemaNodes,
        [],
        selectedViewsNames,
        existingViewNames
      );
    }

    let filteredAndSorted = viewInfos.slice();
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
  };

  const [loading,setLoading] = React.useState<boolean>(false);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);

  const { refreshSchemaConnections } = useVirtualizationHelpers();

  let displayedViews: ViewInfo[] = [];
  const selectedViewNames: string[] = getSelectedViewName(props.selectedViews);

  const handleViewSelectionChange = async (name: string, selected: boolean) => {
    if (selected) {
      for (const viewInfo of displayedViews) {
        if (viewInfo.viewName === name) {
          props.onViewSelected(viewInfo);
        }
      }
    } else {
      props.onViewDeselected(name);
    }
  };

  const {
    resource: schema,
    hasData: hasSchema,
    error,
  } = useVirtualizationConnectionSchema(props.connectionName);

  const [schemaNodes, setSchemaNodes] = React.useState<SchemaNode[]>(schema);

  const toggleRefreshState = async () => {
    setLoading(true);
    try {
      const res = await refreshSchemaConnections(props.connectionName);
      setSchemaNodes(res);
      props.clearSelectedViews();
      pushNotification(
        t('refreshConnectionsSuccess'),
        'success'
      );
      setLoading(false);
    } catch (err) {
      const details = err.message ? err.message : '';
      pushNotification(
        t('refreshConnectionsFailed', {
          details
        }),
        'error'
      );
      setLoading(false);
    }
  };

  React.useEffect(() => {
    if (hasSchema === true) {
      setSchemaNodes(schema);
    }
  }, [hasSchema, schema]);

  return (
    <WithListViewToolbarHelpers
      defaultFilterType={filterByName}
      defaultSortType={sortByName}
    >
      {helpers => {
        const filteredAndSorted = getFilteredAndSortedViewInfos(
          helpers.activeFilters,
          helpers.isSortAscending,
          selectedViewNames,
          props.existingViewNames
        );
        displayedViews = filteredAndSorted.slice();

        return (
          <ViewInfoList
            filterTypes={filterTypes}
            sortTypes={sortTypes}
            resultsCount={filteredAndSorted.length}
            {...helpers}
            i18nEmptyStateInfo={t('emptyStateInfoMessage')}
            i18nEmptyStateTitle={t('emptyStateTitle')}
            i18nName={t('shared:Name')}
            i18nNameFilterPlaceholder={t('shared:nameFilterPlaceholder')}
            i18nRefresh={t('shared:Refresh')}
            i18nLoading={t('shared:Loading')}
            i18nResultsCount={t('shared:resultsCount', {
              count: filteredAndSorted.length,
            })}
            refreshSchemaConnections={toggleRefreshState}
            loading={loading}
          >
            <WithLoader
              error={error !== false}
              loading={!hasSchema}
              loaderChildren={<ViewInfoListSkeleton width={800} />}
              errorChildren={<ApiError error={error as Error} />}
            >
              {() => (
                <ViewInfoListItems
                  filteredAndSorted={filteredAndSorted}
                  onSelectionChanged={handleViewSelectionChange}
                  selectedViewNames={selectedViewNames}
                  handleSelectAll={props.handleSelectAll}
                  i18nUpdate={t('shared:Update')}
                  i18nSelectAll={t('importViewSelectAll', {
                    x: selectedViewNames ? selectedViewNames.length : 0,
                    y: filteredAndSorted ? filteredAndSorted.length : 0,
                  })}
                />
              )}
            </WithLoader>
          </ViewInfoList>
        );
      }}
    </WithListViewToolbarHelpers>
  );
};
