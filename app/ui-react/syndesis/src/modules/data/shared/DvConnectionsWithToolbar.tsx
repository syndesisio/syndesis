import { WithConnections } from '@syndesis/api';
import { Connection, VirtualizationSourceStatus } from '@syndesis/models';
import {
  DvConnectionsListView,
  IActiveFilter,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';
import { DvConnections } from './DvConnections';
import { generateDvConnections } from './VirtualizationUtils';

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
    selectedConn,
    true
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

  return filteredAndSortedConnections;
}

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

  const handleConnectionSelectionChanged = (name: string, selected: boolean) => {
    props.onConnectionSelectionChanged(name, selected);
    setSelectedConnection(selected ? name : '');
  }

  return (
    <WithConnections>
      {({ data, hasData, error }) => (
        <WithListViewToolbarHelpers
          defaultFilterType={filterByName}
          defaultSortType={sortByName}
        >
          {helpers => {
            const filteredAndSortedConnections = getFilteredAndSortedConnections(
              data.connectionsForDisplay,
              props.dvSourceStatuses,
              selectedConnection,
              helpers.activeFilters,
              helpers.currentSortType,
              helpers.isSortAscending
            );

            return (
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
                i18nLinkCreateConnection={t(
                  'shared:linkCreateConnection'
                )}
                i18nResultsCount={t('shared:resultsCount', {
                  count: filteredAndSortedConnections.length,
                })}
              >
                {props.children}
                <DvConnections
                  error={props.error}
                  errorMessage={props.errorMessage}
                  loading={props.loading}
                  connections={filteredAndSortedConnections}
                  initialSelection={selectedConnection}
                  onConnectionSelectionChanged={
                    handleConnectionSelectionChanged
                  }
                />
              </DvConnectionsListView>
            );
          }}
        </WithListViewToolbarHelpers>
      )}
    </WithConnections>
  );
}
