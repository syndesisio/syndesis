import { WithVirtualizations } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  VirtListItem,
  VirtListSkeleton,
  VirtListView,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import resolvers from '../resolvers';

function getFilteredAndSortedVirts(
  virts: RestDataService[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
  isSortAscending: boolean
) {
  let filteredAndSorted = virts;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((virt: RestDataService) =>
      virt.keng__id.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisVirt, thatVirt) => {
    if (isSortAscending) {
      return thisVirt.keng__id.localeCompare(thatVirt.keng__id);
    }

    // sort descending
    return thatVirt.keng__id.localeCompare(thisVirt.keng__id);
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

export function getVirtualizationsHref(baseUrl: string): string {
  return `${baseUrl}`;
}

export default class VirtualizationsPage extends React.Component {
  public filterUndefinedId(virt: RestDataService): boolean {
    return virt.keng__id !== undefined;
  }

  public handleCreateVirt(virtName: H.LocationDescriptor) {
    // TODO: implement handleCreateVirt
    alert('Create virtualization ' + virtName);
  }

  public handleImportVirt(virtName: string) {
    // TODO: implement handleImportVirt
    alert('Import virtualization ' + virtName);
  }

  public handleDeleteVirtualization() {
    // TODO: implement handleCreateVirtualization
    alert('Delete virtualization ');
  }

  public handleEditVirtualization() {
    // TODO: implement handleEditVirtualization
    alert('Edit virtualization ');
  }

  public handleExportVirtualization() {
    // TODO: implement handleExportVirtualization
    alert('Export virtualization ');
  }

  public handleUnpublishVirtualization() {
    // TODO: implement handleUnpublishVirtualization
    alert('Unpublish virtualization ');
  }

  public handlePublishVirtualization() {
    // TODO: implement handlePublishVirtualization
    alert('Publish virtualization ');
  }

  public render() {
    return (
      <WithVirtualizations>
        {({ data, hasData, error }) => (
          <WithListViewToolbarHelpers
            defaultFilterType={filterByName}
            defaultSortType={sortByName}
          >
            {helpers => {
              const filteredAndSorted = getFilteredAndSortedVirts(
                data,
                helpers.activeFilters,
                helpers.currentSortType,
                helpers.isSortAscending
              );

              return (
                <Translation ns={['data', 'shared']}>
                  {t => (
                    <VirtListView
                      filterTypes={filterTypes}
                      sortTypes={sortTypes}
                      {...this.state}
                      resultsCount={filteredAndSorted.length}
                      {...helpers}
                      i18nCreateDataVirt={t(
                        'virtualization.createDataVirtualization'
                      )}
                      i18nCreateDataVirtTip={t(
                        'virtualization.createDataVirtualizationTip'
                      )}
                      i18nDescription={t(
                        'virtualization.virtualizationsPageDescription'
                      )}
                      i18nEmptyStateInfo={t(
                        'virtualization.emptyStateInfoMessage'
                      )}
                      i18nEmptyStateTitle={t('virtualization.emptyStateTitle')}
                      i18nImport={t('shared:Import')}
                      i18nImportTip={t(
                        'virtualization.importVirtualizationTip'
                      )}
                      i18nLinkCreateVirt={t(
                        'virtualization.createDataVirtualization'
                      )}
                      i18nName={t('shared:Name')}
                      i18nNameFilterPlaceholder={t(
                        'shared:nameFilterPlaceholder'
                      )}
                      i18nResultsCount={t('shared:resultsCount', {
                        count: filteredAndSorted.length,
                      })}
                      i18nTitle={t('virtualization.virtualizationsPageTitle')}
                      linkCreateHRef={resolvers.virtualizations.create()}
                      onCreate={this.handleCreateVirt}
                      onImport={this.handleImportVirt}
                    >
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={
                          <VirtListSkeleton
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
                            (virt: RestDataService, index: number) => (
                              <VirtListItem
                                key={index}
                                virtName={virt.keng__id}
                                virtDescription={virt.tko__description}
                                i18nDraft={t('shared:Draft')}
                                i18nDraftTip={t(
                                  'virtualization.draftDataVirtualizationTip'
                                )}
                                i18nEdit={t('shared:Edit')}
                                i18nEditTip={t(
                                  'virtualization.editDataVirtualizationTip'
                                )}
                                i18nPublished={t(
                                  'virtualization.publishedDataVirtualization'
                                )}
                                i18nPublishedTip={t(
                                  'virtualization.publishedDataVirtualizationTip'
                                )}
                                i18nUnpublish={t('shared:Unpublish')}
                                i18nPublish={t('shared:Publish')}
                                onDelete={this.handleDeleteVirtualization}
                                onEdit={this.handleEditVirtualization}
                                onExport={this.handleExportVirtualization}
                                onUnpublish={this.handleUnpublishVirtualization}
                                onPublish={this.handlePublishVirtualization}
                                // TODO: modify komodo service call to add published state
                                isPublished={false}
                              />
                            )
                          )
                        }
                      </WithLoader>
                    </VirtListView>
                  )}
                </Translation>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </WithVirtualizations>
    );
  }
}
