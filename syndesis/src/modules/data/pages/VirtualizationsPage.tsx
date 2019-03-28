import { WithVirtualizationHelpers, WithVirtualizations } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  VirtualizationList,
  VirtualizationListItem,
  VirtualizationListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import resolvers from '../resolvers';

function getFilteredAndSortedVirtualizations(
  virtualizations: RestDataService[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSorted = virtualizations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter(
      (virtualization: RestDataService) =>
        virtualization.keng__id.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort(
    (thisVirtualization, thatVirtualization) => {
      if (isSortAscending) {
        return thisVirtualization.keng__id.localeCompare(
          thatVirtualization.keng__id
        );
      }

      // sort descending
      return thatVirtualization.keng__id.localeCompare(
        thisVirtualization.keng__id
      );
    }
  );

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

export function getVirtualizationsHref(baseUrl: string): string {
  return `${baseUrl}`;
}

export default class VirtualizationsPage extends React.Component {
  public filterUndefinedId(virtualization: RestDataService): boolean {
    return virtualization.keng__id !== undefined;
  }

  public handleImportVirt(virtualizationName: string) {
    // TODO: implement handleImportVirt
    alert('Import virtualization ' + virtualizationName);
  }

  public handleEditVirtualization() {
    // TODO: implement handleEditVirtualization
    alert('Edit virtualization ');
  }

  public handleExportVirtualization() {
    // TODO: implement handleExportVirtualization
    alert('Export virtualization ');
  }

  public render() {
    return (
      // TODO need to retrieve real here
      <WithVirtualizationHelpers username="developer">
        {({
          deleteVirtualization,
          publishVirtualization,
          unpublishServiceVdb,
        }) => {
          const handleDelete = async (virtualizationName: string) => {
            await deleteVirtualization(virtualizationName);
            // TODO: post toast notification
          };
          const handlePublish = async (virtualizationName: string) => {
            await publishVirtualization(virtualizationName);
            // TODO: post toast notification
          };
          const handleUnpublish = async (serviceVdbName: string) => {
            await unpublishServiceVdb(serviceVdbName);
            // TODO: post toast notification
          };
          return (
            <WithVirtualizations>
              {({ data, hasData, error }) => (
                <WithListViewToolbarHelpers
                  defaultFilterType={filterByName}
                  defaultSortType={sortByName}
                >
                  {helpers => {
                    const filteredAndSorted = getFilteredAndSortedVirtualizations(
                      data,
                      helpers.activeFilters,
                      helpers.currentSortType,
                      helpers.isSortAscending
                    );

                    return (
                      <Translation ns={['data', 'shared']}>
                        {t => (
                          <VirtualizationList
                            filterTypes={filterTypes}
                            sortTypes={sortTypes}
                            {...this.state}
                            resultsCount={filteredAndSorted.length}
                            {...helpers}
                            i18nCreateDataVirtualization={t(
                              'virtualization.createDataVirtualization'
                            )}
                            i18nCreateDataVirtualizationTip={t(
                              'virtualization.createDataVirtualizationTip'
                            )}
                            i18nDescription={t(
                              'virtualization.virtualizationsPageDescription'
                            )}
                            i18nEmptyStateInfo={t(
                              'virtualization.emptyStateInfoMessage'
                            )}
                            i18nEmptyStateTitle={t(
                              'virtualization.emptyStateTitle'
                            )}
                            i18nImport={t('shared:Import')}
                            i18nImportTip={t(
                              'virtualization.importVirtualizationTip'
                            )}
                            i18nLinkCreateVirtualization={t(
                              'virtualization.createDataVirtualization'
                            )}
                            i18nName={t('shared:Name')}
                            i18nNameFilterPlaceholder={t(
                              'shared:nameFilterPlaceholder'
                            )}
                            i18nResultsCount={t('shared:resultsCount', {
                              count: filteredAndSorted.length,
                            })}
                            i18nTitle={t(
                              'virtualization.virtualizationsPageTitle'
                            )}
                            linkCreateHRef={resolvers.virtualizations.create()}
                            onImport={this.handleImportVirt}
                            hasListData={data.length > 0}
                          >
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={
                                <VirtualizationListSkeleton
                                  width={800}
                                  style={{
                                    backgroundColor: '#FFF',
                                    marginTop: 30,
                                  }}
                                />
                              }
                              errorChildren={<div>TODO</div>}
                            >
                              {() =>
                                filteredAndSorted.map(
                                  (
                                    virtualization: RestDataService,
                                    index: number
                                  ) => (
                                    <VirtualizationListItem
                                      key={index}
                                      virtualizationName={
                                        virtualization.keng__id
                                      }
                                      virtualizationDescription={
                                        virtualization.tko__description
                                          ? virtualization.tko__description
                                          : ''
                                      }
                                      serviceVdbName={
                                        virtualization.serviceVdbName
                                      }
                                      i18nCancelText={t('shared:Cancel')}
                                      i18nDelete={t('shared:Delete')}
                                      i18nDeleteModalMessage={t(
                                        'virtualization.deleteModalMessage',
                                        { name: virtualization.keng__id }
                                      )}
                                      i18nDeleteModalTitle={t(
                                        'virtualization.deleteModalTitle'
                                      )}
                                      i18nDraft={t('shared:Draft')}
                                      i18nEdit={t('shared:Edit')}
                                      i18nEditTip={t(
                                        'virtualization.editDataVirtualizationTip'
                                      )}
                                      i18nError={t('shared:Error')}
                                      i18nExport={t('shared:Export')}
                                      i18nPublished={t(
                                        'virtualization.publishedDataVirtualization'
                                      )}
                                      i18nUnpublish={t('shared:Unpublish')}
                                      i18nPublish={t('shared:Publish')}
                                      onDelete={handleDelete}
                                      onEdit={this.handleEditVirtualization}
                                      onExport={this.handleExportVirtualization}
                                      onUnpublish={handleUnpublish}
                                      onPublish={handlePublish}
                                      currentPublishedState={
                                        virtualization.publishedState
                                      }
                                      publishLogUrl={''} // TODO set the generated url for the pod
                                      i18nPublishInProgress={t(
                                        'virtualization.publishInProgress'
                                      )}
                                      i18nPublishLogUrlText={t(
                                        'shared:viewLogs'
                                      )}
                                    />
                                  )
                                )
                              }
                            </WithLoader>
                          </VirtualizationList>
                        )}
                      </Translation>
                    );
                  }}
                </WithListViewToolbarHelpers>
              )}
            </WithVirtualizations>
          );
        }}
      </WithVirtualizationHelpers>
    );
  }
}
