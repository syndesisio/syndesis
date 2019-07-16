import { useExtensionHelpers, useExtensions } from '@syndesis/api';
import { Extension } from '@syndesis/models';
import {
  ExtensionListItem,
  ExtensionListSkeleton,
  ExtensionListView,
  IActiveFilter,
  IFilterType,
  ISortType,
  PageSection,
  SimplePageHeader,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError, EntityIcon } from '../../../shared';
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

export const ExtensionsPage: React.FunctionComponent = () => {
  const { pushNotification } = React.useContext(UIContext);
  const { deleteExtension } = useExtensionHelpers();
  const {
    resource: extensionsData,
    hasData: hasExtensionsData,
    error: extensionsError,
  } = useExtensions();
  const { t } = useTranslation(['extensions', 'shared']);

  const filterUndefinedId = (extension: Extension): boolean => {
    return extension.id !== undefined;
  };

  const getUsedByMessage = (extension: Extension): string => {
    // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = extension.uses as number;

    if (numUsedBy === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: numUsedBy });
  };

  const handleDelete = async (extensionId: string) => {
    try {
      await deleteExtension(extensionId);
      pushNotification(t('extension.extensionDeletedMessage'), 'success');
    } catch {
      pushNotification(
        t('extension.errorDeletingExtension', {
          extensionId,
        }),
        'error'
      );
    }
  };

  return (
    <WithListViewToolbarHelpers
      defaultFilterType={filterByName}
      defaultSortType={sortByName}
    >
      {helpers => {
        const filteredAndSorted = getFilteredAndSortedExtensions(
          extensionsData.items,
          helpers.activeFilters,
          helpers.currentSortType,
          helpers.isSortAscending
        );

        return (
          <>
            <SimplePageHeader
              i18nTitle={t('extension.extensionsPageTitle')}
              i18nDescription={t('extension.extensionsPageDescription')}
            />
            <WithLoader
              error={extensionsError !== false}
              loading={!hasExtensionsData}
              loaderChildren={
                <PageSection>
                  <ExtensionListSkeleton
                    width={800}
                    style={{
                      backgroundColor: '#FFF',
                      marginTop: 30,
                    }}
                  />
                </PageSection>
              }
              errorChildren={<ApiError error={extensionsError as Error} />}
            >
              {() => (
                <ExtensionListView
                  filterTypes={filterTypes}
                  sortTypes={sortTypes}
                  linkImportExtension={resolvers.import()}
                  resultsCount={filteredAndSorted.length}
                  {...helpers}
                  i18nTitle={''}
                  i18nDescription={''}
                  i18nEmptyStateInfo={t('extension.emptyStateInfoMessage')}
                  i18nEmptyStateTitle={t('extension.emptyStateTitle')}
                  i18nLinkImportExtension={t('extension.ImportExtension')}
                  i18nLinkImportExtensionTip={t('extension.importExtensionTip')}
                  i18nName={t('shared:Name')}
                  i18nNameFilterPlaceholder={t('shared:nameFilterPlaceholder')}
                  i18nResultsCount={t('shared:resultsCount', {
                    count: filteredAndSorted.length,
                  })}
                >
                  {filteredAndSorted.length > 0
                    ? filteredAndSorted
                        .filter((extension: Extension) =>
                          filterUndefinedId(extension)
                        )
                        .map((extension: Extension, index: number) => (
                          <ExtensionListItem
                            key={index}
                            detailsPageLink={resolvers.extension.details({
                              extension,
                            })}
                            extensionDescription={extension.description}
                            extensionIcon={
                              <EntityIcon
                                entity={extension}
                                alt={extension.name}
                                width={46}
                              />
                            }
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
                            i18nDeleteTip={t('extension.deleteExtensionTip')}
                            i18nDetails={t('shared:Details')}
                            i18nDetailsTip={t('extension.detailsExtensionTip')}
                            i18nExtensionType={getExtensionTypeName(extension)}
                            i18nUpdate={t('shared:Update')}
                            i18nUpdateTip={t('extension.updateExtensionTip')}
                            i18nUsedByMessage={getUsedByMessage(extension)}
                            linkUpdateExtension={resolvers.extension.update({
                              extension,
                            })}
                            onDelete={handleDelete}
                            usedBy={
                              // TODO: Schema is currently wrong as it has 'uses` as an OptionalInt. Remove cast when schema is fixed.
                              extension.uses as number
                            }
                          />
                        ))
                    : undefined}
                </ExtensionListView>
              )}
            </WithLoader>
          </>
        );
      }}
    </WithListViewToolbarHelpers>
  );
};
