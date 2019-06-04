import { Connection } from '@syndesis/models';
import {
  ConnectionsListView,
  IActiveFilter,
  IConnectionsListViewProps,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import resolvers from '../../connections/resolvers';
import {
  Connections,
  IConnectionsProps,
} from '../../connections/components/Connections';

function getFilteredAndSortedConnections(
  connections: Connection[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
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

export interface IUIStepsWithToolbarProps
  extends IConnectionsProps,
    Pick<IConnectionsListViewProps, 'createConnectionButtonStyle'> {
  children?: any;
}

export class UIStepsWithToolbar extends React.Component<
  IUIStepsWithToolbarProps
> {
  public static defaultProps = {
    includeHidden: false,
  };

  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
          <WithListViewToolbarHelpers
            defaultFilterType={filterByName}
            defaultSortType={sortByName}
          >
            {helpers => {
              const connectionString = 'connection';
              let filteredAndSortedConnections = getFilteredAndSortedConnections(
                this.props.connections,
                helpers.activeFilters,
                helpers.currentSortType,
                helpers.isSortAscending
              );

              filteredAndSortedConnections = filteredAndSortedConnections.map(
                connection => {
                  return connection[connectionString];
                }
              );

              return (
                <ConnectionsListView
                  createConnectionButtonStyle={
                    this.props.createConnectionButtonStyle
                  }
                  linkToConnectionCreate={resolvers.create.selectConnector()}
                  filterTypes={filterTypes}
                  sortTypes={sortTypes}
                  resultsCount={filteredAndSortedConnections.length}
                  {...helpers}
                  i18nLinkCreateConnection={t('shared:linkCreateConnection')}
                  i18nResultsCount={t('shared:resultsCount', {
                    count: filteredAndSortedConnections.length,
                  })}
                >
                  {this.props.children}
                  <Connections
                    error={this.props.error}
                    includeConnectionMenu={this.props.includeConnectionMenu}
                    loading={this.props.loading}
                    connections={filteredAndSortedConnections}
                    getConnectionHref={this.props.getConnectionHref}
                    getConnectionEditHref={this.props.getConnectionEditHref}
                  />
                </ConnectionsListView>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </Translation>
    );
  }
}
