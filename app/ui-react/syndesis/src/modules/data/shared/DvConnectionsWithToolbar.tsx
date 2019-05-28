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
import { Translation } from 'react-i18next';
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
  loading: boolean;
  dvSourceStatuses: VirtualizationSourceStatus[];
  onConnectionSelectionChanged: (name: string, selected: boolean) => void;
  children?: any;
}
export interface IDvConnectionsWithToolbarState {
  selectedConnection: any;
}

export class DvConnectionsWithToolbar extends React.Component<
  IDvConnectionsWithToolbarProps,
  IDvConnectionsWithToolbarState
> {
  public constructor(props: IDvConnectionsWithToolbarProps) {
    super(props);
    this.state = {
      selectedConnection: '', // initial selected connection empty
    };
    this.handleConnectionSelectionChanged = this.handleConnectionSelectionChanged.bind(
      this
    );
  }

  public handleConnectionSelectionChanged(name: string, selected: boolean) {
    this.props.onConnectionSelectionChanged(name, selected);
  }

  public render() {
    return (
      <Translation ns={['shared']}>
        {t => (
          <WithConnections>
            {({ data, hasData, error }) => (
              <WithListViewToolbarHelpers
                defaultFilterType={filterByName}
                defaultSortType={sortByName}
              >
                {helpers => {
                  const filteredAndSortedConnections = getFilteredAndSortedConnections(
                    data.connectionsForDisplay,
                    this.props.dvSourceStatuses,
                    this.state.selectedConnection,
                    helpers.activeFilters,
                    helpers.currentSortType,
                    helpers.isSortAscending
                  );

                  return (
                    <DvConnectionsListView
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
                      {this.props.children}
                      <DvConnections
                        error={this.props.error}
                        loading={this.props.loading}
                        connections={filteredAndSortedConnections}
                        onConnectionSelectionChanged={
                          this.handleConnectionSelectionChanged
                        }
                      />
                    </DvConnectionsListView>
                  );
                }}
              </WithListViewToolbarHelpers>
            )}
          </WithConnections>
        )}
      </Translation>
    );
  }
}
