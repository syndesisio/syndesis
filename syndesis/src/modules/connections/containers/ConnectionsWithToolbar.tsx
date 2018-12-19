import { Connection } from '@syndesis/models';
import {
  ConnectionsListView,
  IActiveFilter,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import i18n from '../../../i18n';
import { Connections, IConnectionsProps } from './Connections';

function getFilteredAndSortedConnections(
  connections: Connection[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSortedConnections = connections;
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

export class ConnectionsWithToolbar extends React.Component<IConnectionsProps> {
  public static defaultProps = {
    includeHidden: false,
  };

  public render() {
    return (
      <NamespacesConsumer ns={['shared']}>
        {t => (
          <WithListViewToolbarHelpers
            defaultFilterType={filterByName}
            defaultSortType={sortByName}
          >
            {helpers => {
              const filteredAndSortedConnections = getFilteredAndSortedConnections(
                this.props.connections,
                helpers.activeFilters,
                helpers.currentSortType,
                helpers.isSortAscending
              );

              return (
                <ConnectionsListView
                  linkToConnectionCreate={'/connections/create'}
                  filterTypes={filterTypes}
                  sortTypes={sortTypes}
                  resultsCount={filteredAndSortedConnections.length}
                  {...helpers}
                  i18nLinkCreateConnection={t('shared:linkCreateConnection')}
                  i18nResultsCount={t('shared:resultsCount', {
                    count: filteredAndSortedConnections.length,
                  })}
                >
                  <Connections
                    error={this.props.error}
                    loading={this.props.loading}
                    connections={this.props.connections}
                    getConnectionHref={this.props.getConnectionHref}
                  />
                </ConnectionsListView>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </NamespacesConsumer>
    );
  }
}
