import { WithApiConnectorHelpers, WithApiConnectors } from '@syndesis/api';
import { Connector } from '@syndesis/models';
import {
  ApiConnectorListItem,
  ApiConnectorListSkeleton,
  ApiConnectorListView,
  IActiveFilter,
  IFilterType,
  ISortType,
  PageSection,
  SimplePageHeader,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../resolvers';
import routes from '../routes';

function getFilteredAndSortedApiConnectors(
  apiConnectors: Connector[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSorted = apiConnectors;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((api: Connector) =>
      api.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisApi, thatApi) => {
    if (isSortAscending) {
      return thisApi.name.localeCompare(thatApi.name);
    }

    // sort descending
    return thatApi.name.localeCompare(thisApi.name);
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

export default class ApiConnectorsPage extends React.Component {
  public filterUndefinedId(api: Connector): boolean {
    return api.id !== undefined;
  }

  public getUsedByMessage(api: Connector): string {
    // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = api.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('apiClientConnectors:usedByOne');
    }

    return i18n.t('apiClientConnectors:usedByMulti', { count: numUsedBy });
  }

  public render() {
    return (
      <UIContext.Consumer>
        {({ pushNotification }) => {
          return (
            <WithApiConnectors>
              {({ data, hasData, error, errorMessage }) => (
                <WithListViewToolbarHelpers
                  defaultFilterType={filterByName}
                  defaultSortType={sortByName}
                >
                  {helpers => {
                    const filteredAndSorted = getFilteredAndSortedApiConnectors(
                      data.items,
                      helpers.activeFilters,
                      helpers.currentSortType,
                      helpers.isSortAscending
                    );

                    return (
                      <Translation ns={['apiClientConnectors', 'shared']}>
                        {t => (
                          <>
                            <SimplePageHeader
                              i18nTitle={t('apiConnectorsPageTitle')}
                              i18nDescription={t(
                                'apiConnectorsPageDescription'
                              )}
                            />
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={
                                <PageSection>
                                  <ApiConnectorListSkeleton
                                    width={800}
                                    style={{
                                      backgroundColor: '#FFF',
                                      marginTop: 30,
                                    }}
                                  />
                                </PageSection>
                              }
                              errorChildren={<ApiError error={errorMessage!} />}
                            >
                              {() => (
                                <ApiConnectorListView
                                  filterTypes={filterTypes}
                                  sortTypes={sortTypes}
                                  linkCreateApiConnector={routes.create.upload}
                                  resultsCount={filteredAndSorted.length}
                                  {...helpers}
                                  i18nTitle={''}
                                  i18nDescription={''}
                                  i18nEmptyStateInfo={t('emptyStateInfo')}
                                  i18nEmptyStateTitle={t('CreateApiConnector')}
                                  i18nLinkCreateApiConnector={t(
                                    'CreateApiConnector'
                                  )}
                                  i18nLinkCreateApiConnectorTip={t(
                                    'createApiConnectorTip'
                                  )}
                                  i18nName={t('shared:Name')}
                                  i18nResultsCount={t('shared:resultsCount', {
                                    count: filteredAndSorted.length,
                                  })}
                                >
                                  {filteredAndSorted.length > 0 ? (
                                    <WithApiConnectorHelpers>
                                      {({ deleteApiConnector }) => {
                                        const handleDelete = async (
                                          apiConnectorId: string
                                        ) => {
                                          try {
                                            await deleteApiConnector(
                                              apiConnectorId
                                            );
                                          } catch {
                                            pushNotification(
                                              t('errorDeletingApiConnector', {
                                                connectorId: apiConnectorId,
                                              }),
                                              'error'
                                            );
                                          }
                                        };

                                        return filteredAndSorted
                                          .filter((api: Connector) =>
                                            this.filterUndefinedId(api)
                                          )
                                          .map(
                                            (
                                              apiConnector: Connector,
                                              index: number
                                            ) => (
                                              <ApiConnectorListItem
                                                key={index}
                                                apiConnectorId={
                                                  apiConnector.id as string
                                                }
                                                apiConnectorDescription={
                                                  apiConnector.description
                                                }
                                                apiConnectorIcon={
                                                  apiConnector.icon
                                                }
                                                apiConnectorName={
                                                  apiConnector.name
                                                }
                                                detailsPageLink={resolvers.apiConnector.details(
                                                  { apiConnector }
                                                )}
                                                i18nCancelLabel={t(
                                                  'shared:Cancel'
                                                )}
                                                i18nDelete={t('shared:Delete')}
                                                i18nDeleteModalMessage={t(
                                                  'deleteModalMessage',
                                                  { name: apiConnector.name }
                                                )}
                                                i18nDeleteModalTitle={t(
                                                  'deleteModalTitle'
                                                )}
                                                i18nDetails={t(
                                                  'shared:Details'
                                                )}
                                                i18nDetailsTip={t(
                                                  'detailsApiConnectorTip'
                                                )}
                                                i18nUsedByMessage={this.getUsedByMessage(
                                                  apiConnector
                                                )}
                                                onDelete={handleDelete}
                                                usedBy={
                                                  apiConnector.uses as number
                                                }
                                              />
                                            )
                                          );
                                      }}
                                    </WithApiConnectorHelpers>
                                  ) : (
                                    undefined
                                  )}
                                </ApiConnectorListView>
                              )}
                            </WithLoader>
                          </>
                        )}
                      </Translation>
                    );
                  }}
                </WithListViewToolbarHelpers>
              )}
            </WithApiConnectors>
          );
        }}
      </UIContext.Consumer>
    );
  }
}
