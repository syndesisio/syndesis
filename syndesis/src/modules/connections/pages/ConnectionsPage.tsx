import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
  ConnectionsListView,
  IActiveFilter,
  IFilterType,
  ISortType,
} from '@syndesis/ui';
import {
  getConnectionIcon,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import i18n from '../../../i18n';

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

export default class ConnectionsPage extends React.Component {
  public render() {
    return (
      <WithConnections>
        {({ data, hasData, error }) => (
          <NamespacesConsumer ns={['shared']}>
            {t => (
              <WithListViewToolbarHelpers
                defaultFilterType={filterByName}
                defaultSortType={sortByName}
              >
                {helpers => {
                  const filteredAndSortedConnections = getFilteredAndSortedConnections(
                    data.items,
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
                      i18nLinkCreateConnection={t(
                        'shared:linkCreateConnection'
                      )}
                      i18nResultsCount={t('shared:resultsCount', {
                        count: filteredAndSortedConnections.length,
                      })}
                    >
                      <ConnectionsGrid>
                        <WithLoader
                          error={error}
                          loading={!hasData}
                          loaderChildren={
                            <>
                              {new Array(5).fill(0).map((_, index) => (
                                <ConnectionsGridCell key={index}>
                                  <ConnectionSkeleton />
                                </ConnectionsGridCell>
                              ))}
                            </>
                          }
                          errorChildren={<div>TODO</div>}
                        >
                          {() =>
                            filteredAndSortedConnections.map((c, index) => (
                              <ConnectionsGridCell key={index}>
                                <ConnectionCard
                                  name={c.name}
                                  description={c.description || ''}
                                  icon={getConnectionIcon(
                                    c,
                                    process.env.PUBLIC_URL
                                  )}
                                />
                              </ConnectionsGridCell>
                            ))
                          }
                        </WithLoader>
                      </ConnectionsGrid>
                    </ConnectionsListView>
                  );
                }}
              </WithListViewToolbarHelpers>
            )}
          </NamespacesConsumer>
        )}
      </WithConnections>
    );
  }
}
