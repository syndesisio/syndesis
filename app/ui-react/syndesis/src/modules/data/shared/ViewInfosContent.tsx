import { useVirtualizationConnectionSchema, useVirtualizationHelpers } from '@syndesis/api';
import { SchemaNode, ViewInfo } from '@syndesis/models';
import {
  DvConnectionStatus,
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewInfoList,
  ViewInfoListItems,
  ViewInfoListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { generateAllViewInfos, getDateAndTimeDisplay } from './VirtualizationUtils';

function getFilteredAndSortedViewInfos(
  schemaNodes: SchemaNode[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean,
  selectedViewNames: string[],
  existingViewNames: string[]
) {
  const viewInfos: any[] = [];
  if (schemaNodes && schemaNodes.length > 0) {
    generateAllViewInfos(
      viewInfos,
      schemaNodes,
      [],
      selectedViewNames,
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
}

const getSelectedViewName = (selectedViews: ViewInfo[]): string[] => {
  return selectedViews.map(view => view.viewName);
};

export interface IViewInfosContentProps {
  connectionLoading: boolean;
  connectionName: string;
  connectionStatus: string;
  connectionStatusMessage: string;
  connectionTeiidName: string;
  existingViewNames: string[];
  connectionLastLoad: number;
  onViewSelected: (view: ViewInfo) => void;
  onViewDeselected: (viewName: string) => void;
  selectedViews: ViewInfo[];
  handleSelectAll: (isSelected: boolean, AllViewInfo: any[]) => void;
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

export const ViewInfosContent: React.FunctionComponent<
  IViewInfosContentProps
> = props => {
  const { t } = useTranslation(['data', 'shared']);
  /**
   * Context that broadcasts global notifications.
   */
  const { pushNotification } = React.useContext(UIContext);

  const [schemaList, setSchemaList] = React.useState<SchemaNode[]>([]);
  const [lastSchemaRefresh, setLastSchemaRefresh] = React.useState(0);

  const [lastSchemaRefreshMsg, setLastSchemaRefreshMsg] = React.useState<string>(t('schemaLastRefresh', {
    refreshTime: getDateAndTimeDisplay(props.connectionLastLoad),
  }));

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
    refreshConnectionSchema,
  } = useVirtualizationHelpers();

  /**
   * Callback that triggers refresh of the connection schema
   * @param connectionName the name of the connection
   */
  const handleRefreshSchema = async (connectionName: string) => {
    try {
      pushNotification(
        t('refreshConnectionSchemaStarted', {
          name: connectionName,
        }),
        'info'
      );
      await refreshConnectionSchema(props.connectionTeiidName);
    } catch (error) {
      const details = error.message ? error.message : '';
      // inform user of error
      pushNotification(
        t('refreshConnectionSchemaFailed', {
          details,
          name: connectionName,
        }),
        'error'
      );
    }
  };

  const {
    resource: schema,
    hasData,
    error,
    read,
  } = useVirtualizationConnectionSchema(props.connectionTeiidName);

  React.useEffect(()=>{
    if (hasData) {
      const rootNode = schema.find((node: { name: string; }) => node.name === props.connectionTeiidName);
      if (rootNode) {
        setSchemaList(rootNode.children);
      }
    }
  },[schema, props.connectionTeiidName, hasData]);

  React.useEffect(() => {
    if(props.connectionLastLoad > lastSchemaRefresh) {
      read();
      setLastSchemaRefresh(props.connectionLastLoad);
      setLastSchemaRefreshMsg(t('schemaLastRefresh', {
        refreshTime: getDateAndTimeDisplay(props.connectionLastLoad),
      }));
    }
  }, [props.connectionLastLoad, lastSchemaRefresh, read, setLastSchemaRefresh, t]);

  return (
    <WithListViewToolbarHelpers
      defaultFilterType={filterByName}
      defaultSortType={sortByName}
    >
      {helpers => {
        const filteredAndSorted = getFilteredAndSortedViewInfos(
          schemaList,
          helpers.activeFilters,
          helpers.currentSortType,
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
            connectionLoading={props.connectionLoading}
            connectionName={props.connectionName}
            connectionStatus={
              <DvConnectionStatus
                dvStatus={props.connectionStatus}
                dvStatusMessage={props.connectionStatusMessage}
                i18nRefreshInProgress={t('refreshInProgress')}
                i18nStatusErrorPopoverTitle={t('connectionStatusPopoverTitle')}
                i18nStatusErrorPopoverLink={t('connectionStatusPopoverLink')}
                loading={props.connectionLoading}
              />
            }
            i18nEmptyStateInfo={t('emptyStateInfoMessage')}
            i18nEmptyStateTitle={t('emptyStateTitle')}
            i18nName={t('shared:Name')}
            i18nNameFilterPlaceholder={t('shared:nameFilterPlaceholder')}
            i18nLastUpdatedMessage={lastSchemaRefreshMsg}
            i18nRefresh={t('shared:Refresh')}
            i18nResultsCount={t('shared:resultsCount', {
              count: filteredAndSorted.length,
            })}
            refreshConnectionSchema={handleRefreshSchema}
          >
            <WithLoader
              error={error !== false}
              loading={schemaList.length===0}
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