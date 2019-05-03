import { WithApiConnectors } from '@syndesis/api';
import { Connector } from '@syndesis/models';
import {
  ApiConnectorListItem,
  ApiConnectorListSkeleton,
  ApiConnectorListView,
  IActiveFilter,
  IFilterType,
  ISortType,
  PageSection,
} from '@syndesis/ui';
import {
  optionalIntValue,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import routes from '../routes';
import CustomizationsNavBar from '../shared/CustomizationsNavBar';

function getFilteredAndSortedApiConnectors(
  apiConnectors: Connector[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
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
    const numUsedBy = optionalIntValue(api.uses);

    if (numUsedBy === 1) {
      return i18n.t('customizations:usedByOne');
    }

    return i18n.t('customizations:usedByMulti', { count: numUsedBy });
  }

  public handleDelete(apiConnectorId: string) {
    // TODO: implement handleDelete
    alert('Delete API client connector ' + apiConnectorId);
  }

  public handleDetails(apiConnectorId: string) {
    // TODO: implement handleDetails
    alert('Show details of API client connector ' + apiConnectorId);
  }

  public render() {
    return (
      <WithApiConnectors>
        {({ data, hasData, error }) => (
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
                <Translation ns={['customizations', 'shared']}>
                  {t => (
                    <>
                      <CustomizationsNavBar />
                      <PageSection variant={'light'}>
                        <h1 className="pf-c-title pf-m-xl">
                          {t('apiConnector.apiConnectorsPageTitle')}
                        </h1>
                        <p
                          dangerouslySetInnerHTML={{
                            __html: t(
                              'apiConnector.apiConnectorsPageDescription'
                            ),
                          }}
                        />
                      </PageSection>
                      <ApiConnectorListView
                        filterTypes={filterTypes}
                        sortTypes={sortTypes}
                        {...this.state}
                        linkCreateApiConnector={
                          routes.apiConnectors.create.upload
                        }
                        resultsCount={filteredAndSorted.length}
                        {...helpers}
                        i18nTitle={''}
                        i18nDescription={''}
                        i18nEmptyStateInfo={t('apiConnector.emptyStateInfo')}
                        i18nEmptyStateTitle={t(
                          'apiConnector.CreateApiConnector'
                        )}
                        i18nLinkCreateApiConnector={t(
                          'apiConnector.CreateApiConnector'
                        )}
                        i18nLinkCreateApiConnectorTip={t(
                          'apiConnector.createApiConnectorTip'
                        )}
                        i18nName={t('shared:Name')}
                        i18nResultsCount={t('shared:resultsCount', {
                          count: filteredAndSorted.length,
                        })}
                      >
                        <WithLoader
                          error={error}
                          loading={!hasData}
                          loaderChildren={
                            <ApiConnectorListSkeleton
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
                            filteredAndSorted
                              .filter((api: Connector) =>
                                this.filterUndefinedId(api)
                              )
                              .map((api: Connector, index: number) => (
                                <ApiConnectorListItem
                                  key={index}
                                  apiConnectorId={api.id as string}
                                  apiConnectorDescription={api.description}
                                  apiConnectorIcon={api.icon}
                                  apiConnectorName={api.name}
                                  i18nDelete={t('shared:Delete')}
                                  i18nDetails={t('shared:Details')}
                                  i18nDetailsTip={t(
                                    'apiConnector.detailsApiConnectorTip'
                                  )}
                                  i18nUsedByMessage={this.getUsedByMessage(api)}
                                  onDelete={this.handleDelete}
                                  onDetails={this.handleDetails}
                                  usedBy={optionalIntValue(api.uses)}
                                />
                              ))
                          }
                        </WithLoader>
                      </ApiConnectorListView>
                    </>
                  )}
                </Translation>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </WithApiConnectors>
    );
  }
}
