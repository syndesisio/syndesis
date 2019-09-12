import { useConnections } from '@syndesis/api';
import { Connection, VirtualizationSourceStatus } from '@syndesis/models';
import {
  DvConnectionsGridCell,
  DvConnectionSkeleton,
  DvConnectionsListView,
  DvConnectionsToolbarSkeleton,
  IActiveFilter,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import { DvConnections } from './DvConnections';
import { generateDvConnections } from './VirtualizationUtils';

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: i18n.t('shared:filterByNamePlaceholder'),
  title: i18n.t('shared:Name'),
} as IFilterType;

const filterTypes = [filterByName];

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;

const sortTypes: ISortType[] = [sortByName];

export interface IDvConnectionsWithToolbarProps {
  error: boolean;
  errorMessage?: string;
  loading: boolean;
  dvSourceStatuses: VirtualizationSourceStatus[];
  onConnectionSelectionChanged: (name: string, selected: boolean) => void;
  children?: any;
}

export const DvConnectionsWithToolbar: React.FunctionComponent<
  IDvConnectionsWithToolbarProps
> = props => {
  const { t } = useTranslation(['data', 'shared']);
  const [selectedConnection, setSelectedConnection] = React.useState('');

  function getFilteredAndSortedConnections(
    connections: Connection[],
    dvSourceStatuses: VirtualizationSourceStatus[],
    selectedConn: string,
    activeFilters: IActiveFilter[],
    currentSortType: ISortType,
    isSortAscending: boolean
  ) {
    // Connections are adjusted to supply dvStatus and selection
    let filteredAndSortedConnections = generateDvConnections(
      connections,
      dvSourceStatuses,
      selectedConn
    );
    activeFilters.forEach((filter: IActiveFilter) => {
      const valueToLower = filter.value.toLowerCase();
      filteredAndSortedConnections = filteredAndSortedConnections.filter(
        (c: Connection) => c.name.toLowerCase().includes(valueToLower)
      );
    });

    filteredAndSortedConnections = filteredAndSortedConnections.sort(
      (miA, miB) => {
        const left = isSortAscending ? miA : miB;
        const right = isSortAscending ? miB : miA;
        return left.name.localeCompare(right.name);
      }
    );

    // setLoaded(true);
    return filteredAndSortedConnections;
  }

  const handleConnectionSelectionChanged = (
    name: string,
    selected: boolean
  ) => {
    props.onConnectionSelectionChanged(name, selected);
    setSelectedConnection(selected ? name : '');
  };

  const {
    resource: connectionsData,
    hasData: hasConnectionsData,
    error: connectionsError,
  } = useConnections();

  return (
    <WithListViewToolbarHelpers
      defaultFilterType={filterByName}
      defaultSortType={sortByName}
    >
      {helpers => {
        const filteredAndSortedConnections = getFilteredAndSortedConnections(
          connectionsData.connectionsForDisplay,
          props.dvSourceStatuses,
          selectedConnection,
          helpers.activeFilters,
          helpers.currentSortType,
          helpers.isSortAscending
        );

        return (
          <WithLoader
            error={props.error || connectionsError !== false}
            loading={props.loading || !hasConnectionsData}
            loaderChildren={
              <>
                <DvConnectionsToolbarSkeleton />
                {new Array(5).fill(0).map((_, index) => (
                  <DvConnectionsGridCell key={index}>
                    <DvConnectionSkeleton />
                  </DvConnectionsGridCell>
                ))}
              </>
            }
            errorChildren={
              <ApiError
                error={props.errorMessage || (connectionsError as Error)}
              />
            }
          >
            {() => (
              <DvConnectionsListView
                i18nEmptyStateInfo={t(
                  'virtualization.activeConnectionsEmptyStateInfo'
                )}
                i18nEmptyStateTitle={t(
                  'virtualization.activeConnectionsEmptyStateTitle'
                )}
                linkToConnectionCreate={resolvers.connections.create.selectConnector()}
                filterTypes={filterTypes}
                sortTypes={sortTypes}
                resultsCount={filteredAndSortedConnections.length}
                {...helpers}
                i18nLinkCreateConnection={t('shared:linkCreateConnection')}
                i18nResultsCount={t('shared:resultsCount', {
                  count: filteredAndSortedConnections.length,
                })}
              >
                {props.children}
                {filteredAndSortedConnections.length > 0 && (
                  <DvConnections
                    connections={filteredAndSortedConnections}
                    initialSelection={selectedConnection}
                    onConnectionSelectionChanged={
                      handleConnectionSelectionChanged
                    }
                  />
                )}
              </DvConnectionsListView>
            )}
          </WithLoader>
        );
      }}
    </WithListViewToolbarHelpers>
  );
};
