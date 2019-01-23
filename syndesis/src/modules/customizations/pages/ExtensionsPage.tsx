import { WithExtensions } from '@syndesis/api';
import { Extension } from '@syndesis/models';
import {
  CustomizationsExtensionListItem,
  CustomizationsExtensionListSkeleton,
  CustomizationsExtensionListView,
  IActiveFilter,
  IFilterType,
  ISortType,
  NavLinkTab,
} from '@syndesis/ui';
import {
  optionalIntValue,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import { Grid } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import i18n from '../../../i18n';

function getFilteredAndSortedExtensions(
  extensions: Extension[],
  activeFilters: IActiveFilter[],
  currentSortType: string,
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

  public getTypeName(extension: Extension) {
    const type = extension.extensionType;

    if ('Steps' === type) {
      return i18n.t('customizations:extension.StepExtension');
    }

    if ('Connectors' === type) {
      return i18n.t('customizations:extension.ConnectorExtension');
    }

    if ('Libraries' === type) {
      return i18n.t('customizations:extension.LibraryExtension');
    }

    return i18n.t('customizations:extension.unknownExtensionType');
  }

  public getUsedByMessage(extension: Extension): string {
    const numUsedBy = optionalIntValue(extension.uses);

    if (numUsedBy === 1) {
      return i18n.t('customizations:usedByOne');
    }

    return i18n.t('customizations:usedByMulti', { count: numUsedBy });
  }

  public handleDelete(extensionId: string) {
    // TODO: implement handleDelete
    alert('Delete extension ' + extensionId);
  }

  public handleDetails(extensionId: string) {
    // TODO: implement handleDetails
    alert('Show details of extension ' + extensionId);
  }

  public handleUpdate(extensionId: string) {
    // TODO: implement handleUpdate
    alert('Update extension ' + extensionId);
  }

  public render() {
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
                <NamespacesConsumer ns={['customizations', 'shared']}>
                  {t => (
                    <Grid fluid={true}>
                      <Grid.Row
                        style={{
                          borderBottom: '1px solid #d1d1d1',
                          paddingBottom: 0,
                        }}
                      >
                        <Grid.Col xs={6} md={3}>
                          <NavLinkTab
                            disableLink={false}
                            i18nLinkTitle={t(
                              'apiConnector.apiConnectorsPageTitle'
                            )}
                            toLink={'/customizations/api-connector'}
                          />
                        </Grid.Col>
                        <Grid.Col>
                          <NavLinkTab
                            disableLink={true}
                            i18nLinkTitle={t('extension.extensionsPageTitle')}
                            toLink={'/customizations/extensions'}
                          />
                        </Grid.Col>
                      </Grid.Row>
                      <Grid.Row>
                        <CustomizationsExtensionListView
                          filterTypes={filterTypes}
                          sortTypes={sortTypes}
                          {...this.state}
                          linkImportExtension={'/extensions/import'}
                          resultsCount={filteredAndSorted.length}
                          {...helpers}
                          i18nDescription={t(
                            'extension.extensionsPageDescription'
                          )}
                          i18nEmptyStateInfo={t(
                            'extension.emptyStateInfoMessage'
                          )}
                          i18nEmptyStateTitle={t('extension.emptyStateTitle')}
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
                          i18nTitle={t('extension.extensionsPageTitle')}
                        >
                          <WithLoader
                            error={error}
                            loading={!hasData}
                            loaderChildren={
                              <CustomizationsExtensionListSkeleton
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
                              data.items
                                .filter((extension: Extension) =>
                                  this.filterUndefinedId(extension)
                                )
                                .map((extension: Extension, index: number) => (
                                  <CustomizationsExtensionListItem
                                    key={index}
                                    extensionDescription={extension.description}
                                    extensionIcon={extension.icon}
                                    extensionId={extension.id}
                                    extensionName={extension.name}
                                    i18nDelete={t('shared:Delete')}
                                    i18nDeleteTip={t(
                                      'extension.deleteExtensionTip'
                                    )}
                                    i18nDetails={t('shared:Details')}
                                    i18nDetailsTip={t(
                                      'extension.detailsExtensionTip'
                                    )}
                                    i18nExtensionType={this.getTypeName(
                                      extension
                                    )}
                                    i18nUpdate={t('shared:Update')}
                                    i18nUpdateTip={t(
                                      'extension.updateExtensionTip'
                                    )}
                                    i18nUsedByMessage={this.getUsedByMessage(
                                      extension
                                    )}
                                    onDelete={this.handleDelete}
                                    onDetails={this.handleDetails}
                                    onUpdate={this.handleUpdate}
                                    usedBy={optionalIntValue(extension.uses)}
                                  />
                                ))
                            }
                          </WithLoader>
                          \{' '}
                        </CustomizationsExtensionListView>
                      </Grid.Row>
                    </Grid>
                  )}
                </NamespacesConsumer>
              );
            }}
          </WithListViewToolbarHelpers>
        )}
      </WithExtensions>
    );
  }
}
