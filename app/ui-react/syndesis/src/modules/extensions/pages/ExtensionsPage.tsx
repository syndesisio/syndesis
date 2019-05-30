import { WithExtensionHelpers, WithExtensions } from '@syndesis/api';
import { Extension } from '@syndesis/models';
import {
  ExtensionListItem,
  ExtensionListSkeleton,
  ExtensionListView,
  IActiveFilter,
  IFilterType,
  ISortType,
  SimplePageHeader,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../resolvers';
import { getExtensionTypeName } from '../utils';

function getFilteredAndSortedExtensions(
  extensions: Extension[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSorted = extensions;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter((extension: Extension) =>
      extension.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisExtension, thatExtension) => {
    if (isSortAscending) {
      return thisExtension.name.localeCompare(thatExtension.name);
    }

    // sort descending
    return thatExtension.name.localeCompare(thisExtension.name);
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

export default class ExtensionsPage extends React.Component {
  public filterUndefinedId(extension: Extension): boolean {
    return extension.id !== undefined;
  }

  public getUsedByMessage(extension: Extension): string {
    // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = extension.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('extensions:usedByOne');
    }

    return i18n.t('extensions:usedByMulti', { count: numUsedBy });
  }

  public render() {
    return (
      <WithExtensionHelpers>
        {({ deleteExtension }) => {
          const handleDelete = async (extensionId: string) => {
            await deleteExtension(extensionId);
            // TODO: post toast notification
          };
          return (
            <WithExtensions>
              {({ data, hasData, error }) => (
                <WithListViewToolbarHelpers
                  defaultFilterType={filterByName}
                  defaultSortType={sortByName}
                >
                  {helpers => {
                    const filteredAndSorted = getFilteredAndSortedExtensions(
                      data.items,
                      helpers.activeFilters,
                      helpers.currentSortType,
                      helpers.isSortAscending
                    );

                    return (
                      <Translation ns={['extensions', 'shared']}>
                        {t => (
                          <>
                            <SimplePageHeader
                              i18nTitle={t('extension.extensionsPageTitle')}
                              i18nDescription={t(
                                'extension.extensionsPageDescription'
                              )}
                            />
                            <ExtensionListView
                              filterTypes={filterTypes}
                              sortTypes={sortTypes}
                              linkImportExtension={resolvers.import()}
                              resultsCount={filteredAndSorted.length}
                              {...helpers}
                              i18nTitle={''}
                              i18nDescription={''}
                              i18nEmptyStateInfo={t(
                                'extension.emptyStateInfoMessage'
                              )}
                              i18nEmptyStateTitle={t(
                                'extension.emptyStateTitle'
                              )}
                              i18nLinkImportExtension={t(
                                'extension.ImportExtension'
                              )}
                              i18nLinkImportExtensionTip={t(
                                'extension.importExtensionTip'
                              )}
                              i18nName={t('shared:Name')}
                              i18nNameFilterPlaceholder={t(
                                'shared:nameFilterPlaceholder'
                              )}
                              i18nResultsCount={t('shared:resultsCount', {
                                count: filteredAndSorted.length,
                              })}
                            >
                              <WithLoader
                                error={error}
                                loading={!hasData}
                                loaderChildren={
                                  <ExtensionListSkeleton
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
                                    .filter((extension: Extension) =>
                                      this.filterUndefinedId(extension)
                                    )
                                    .map(
                                      (extension: Extension, index: number) => (
                                        <ExtensionListItem
                                          key={index}
                                          detailsPageLink={resolvers.extension.details(
                                            { extension }
                                          )}
                                          extensionDescription={
                                            extension.description
                                          }
                                          extensionIcon={extension.icon}
                                          extensionId={extension.id!}
                                          extensionName={extension.name}
                                          i18nCancelText={t('shared:Cancel')}
                                          i18nDelete={t('shared:Delete')}
                                          i18nDeleteModalMessage={t(
                                            'extension.deleteModalMessage',
                                            { name: extension.name }
                                          )}
                                          i18nDeleteModalTitle={t(
                                            'extension.deleteModalTitle'
                                          )}
                                          i18nDeleteTip={t(
                                            'extension.deleteExtensionTip'
                                          )}
                                          i18nDetails={t('shared:Details')}
                                          i18nDetailsTip={t(
                                            'extension.detailsExtensionTip'
                                          )}
                                          i18nExtensionType={getExtensionTypeName(
                                            extension
                                          )}
                                          i18nUpdate={t('shared:Update')}
                                          i18nUpdateTip={t(
                                            'extension.updateExtensionTip'
                                          )}
                                          i18nUsedByMessage={this.getUsedByMessage(
                                            extension
                                          )}
                                          linkUpdateExtension={resolvers.extension.update(
                                            { extension }
                                          )}
                                          onDelete={handleDelete}
                                          usedBy={
                                            // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
                                            extension.uses as number
                                          }
                                        />
                                      )
                                    )
                                }
                              </WithLoader>
                            </ExtensionListView>
                          </>
                        )}
                      </Translation>
                    );
                  }}
                </WithListViewToolbarHelpers>
              )}
            </WithExtensions>
          );
        }}
      </WithExtensionHelpers>
    );
  }
}
