import { WithVirtualizationHelpers, WithVirtualizations } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  PageSection,
  SimplePageHeader,
  VirtualizationList,
  VirtualizationListItem,
  VirtualizationListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../resolvers';
import {
  getOdataUrl,
  getPublishingDetails,
} from '../shared/VirtualizationUtils';

function getFilteredAndSortedVirtualizations(
  virtualizations: RestDataService[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
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

export class VirtualizationsPage extends React.Component {
  public filterUndefinedId(virtualization: RestDataService): boolean {
    return virtualization.keng__id !== undefined;
  }

  public handleImportVirt(virtualizationName: string) {
    // TODO: implement handleImportVirt
    alert('Import virtualization ' + virtualizationName);
  }

  /* TD-636: Commented out for TP
  public handleExportVirtualization() {
    // TODO: implement handleExportVirtualization
    alert('Export virtualization ');
  } */

  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <AppContext.Consumer>
            {({ config }) => {
              return (
                <UIContext.Consumer>
                  {({ pushNotification }) => {
                    if (config.datavirt.enabled === 0) {
                      return (
                        <SimplePageHeader
                          i18nTitle={t(
                            'virtualization.virtualizationsPageTitle'
                          )}
                          i18nDescription={t(
                            'virtualization.virtualizationsDisabled'
                          )}
                        />
                      )
                    }

                    return (
                      <WithVirtualizations>
                        {({ data, hasData, error, read }) => {
                          return (
                            <WithVirtualizationHelpers>
                              {({
                                deleteVirtualization,
                                publishVirtualization,
                                unpublishServiceVdb,
                              }) => {
                                const handleDelete = async (
                                  virtualizationName: string
                                ) => {
                                  try {
                                    await deleteVirtualization(
                                      virtualizationName
                                    ).then(read);
                                    pushNotification(
                                      t(
                                        'virtualization.deleteVirtualizationSuccess',
                                        { name: virtualizationName }
                                      ),
                                      'success'
                                    );
                                  } catch (error) {
                                    const details = error.message
                                      ? error.message
                                      : '';
                                    pushNotification(
                                      t(
                                        'virtualization.deleteVirtualizationFailed',
                                        {
                                          details,
                                          name: virtualizationName,
                                        }
                                      ),
                                      'error'
                                    );
                                  }
                                };
                                const handlePublish = async (
                                  virtualizationName: string,
                                  hasViews: boolean
                                ) => {
                                  if (hasViews) {
                                    try {
                                      await publishVirtualization(
                                        virtualizationName
                                      );

                                      pushNotification(
                                        t(
                                          'virtualization.publishVirtualizationSuccess',
                                          { name: virtualizationName }
                                        ),
                                        'success'
                                      );
                                    } catch (error) {
                                      const details = error.error
                                        ? error.error
                                        : '';
                                      pushNotification(
                                        t(
                                          'virtualization.publishVirtualizationFailed',
                                          { name: virtualizationName, details }
                                        ),
                                        'error'
                                      );
                                    }
                                  } else {
                                    pushNotification(
                                      t(
                                        'virtualization.publishVirtualizationNoViews',
                                        { name: virtualizationName }
                                      ),
                                      'error'
                                    );
                                  }
                                };
                                const handleUnpublish = async (
                                  serviceVdbName: string
                                ) => {
                                  try {
                                    await unpublishServiceVdb(serviceVdbName);

                                    pushNotification(
                                      t(
                                        'virtualization.unpublishVirtualizationSuccess',
                                        { name: serviceVdbName }
                                      ),
                                      'success'
                                    );
                                  } catch (error) {
                                    const details = error.message
                                      ? error.message
                                      : '';
                                    pushNotification(
                                      t('virtualization.unpublishFailed', {
                                        details,
                                        name: serviceVdbName,
                                      }),
                                      'error'
                                    );
                                  }
                                };
                                return (
                                  <>
                                    <SimplePageHeader
                                      i18nTitle={t(
                                        'virtualization.virtualizationsPageTitle'
                                      )}
                                      i18nDescription={t(
                                        'virtualization.virtualizationsPageDescription'
                                      )}
                                    />
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
                                          <PageSection
                                            variant={'light'}
                                            noPadding={true}
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
                                              errorChildren={<ApiError />}
                                            >
                                              {() => (
                                                <VirtualizationList
                                                  filterTypes={filterTypes}
                                                  sortTypes={sortTypes}
                                                  resultsCount={
                                                    filteredAndSorted.length
                                                  }
                                                  {...helpers}
                                                  i18nCreateDataVirtualization={t(
                                                    'virtualization.createDataVirtualization'
                                                  )}
                                                  i18nCreateDataVirtualizationTip={t(
                                                    'virtualization.createDataVirtualizationTip'
                                                  )}
                                                  i18nEmptyStateInfo={t(
                                                    'virtualization.emptyStateInfoMessage'
                                                  )}
                                                  i18nEmptyStateTitle={t(
                                                    'virtualization.emptyStateTitle'
                                                  )}
                                                  /* TD-636: Commented out for TP 
                                                  i18nImport={t('shared:Import')}
                                                  i18nImportTip={t(
                                                    'virtualization.importVirtualizationTip'
                                                  )} */
                                                  i18nLinkCreateVirtualization={t(
                                                    'virtualization.createDataVirtualization'
                                                  )}
                                                  i18nName={t('shared:Name')}
                                                  i18nNameFilterPlaceholder={t(
                                                    'shared:nameFilterPlaceholder'
                                                  )}
                                                  i18nResultsCount={t(
                                                    'shared:resultsCount',
                                                    {
                                                      count:
                                                        filteredAndSorted.length,
                                                    }
                                                  )}
                                                  linkCreateHRef={resolvers.virtualizations.create()}
                                                  /* TD-636: Commented out for TP 
                                                  onImport={this.handleImportVirt} */
                                                  hasListData={data.length > 0}
                                                >
                                                  {filteredAndSorted.map(
                                                    (
                                                      virtualization: RestDataService,
                                                      index: number
                                                    ) => {
                                                      const publishingDetails = getPublishingDetails(
                                                        config.consoleUrl,
                                                        virtualization
                                                      );
                                                      return (
                                                        <VirtualizationListItem
                                                          key={index}
                                                          detailsPageLink={resolvers.virtualizations.views.root(
                                                            { virtualization }
                                                          )}
                                                          virtualizationName={
                                                            virtualization.keng__id
                                                          }
                                                          virtualizationDescription={
                                                            virtualization.tko__description
                                                              ? virtualization.tko__description
                                                              : ''
                                                          }
                                                          virtualizationViewNames={
                                                            virtualization.serviceViewDefinitions
                                                          }
                                                          serviceVdbName={
                                                            virtualization.serviceVdbName
                                                          }
                                                          odataUrl={getOdataUrl(
                                                            virtualization
                                                          )}
                                                          i18nCancelText={t(
                                                            'shared:Cancel'
                                                          )}
                                                          i18nDelete={t(
                                                            'shared:Delete'
                                                          )}
                                                          i18nDeleteModalMessage={t(
                                                            'virtualization.deleteModalMessage',
                                                            {
                                                              name:
                                                                virtualization.keng__id,
                                                            }
                                                          )}
                                                          i18nDeleteModalTitle={t(
                                                            'virtualization.deleteModalTitle'
                                                          )}
                                                          i18nDraft={t(
                                                            'shared:Draft'
                                                          )}
                                                          i18nEdit={t(
                                                            'shared:Edit'
                                                          )}
                                                          i18nEditTip={t(
                                                            'virtualization.editDataVirtualizationTip'
                                                          )}
                                                          i18nError={t(
                                                            'shared:Error'
                                                          )}
                                                          /* TD-636: Commented out for TP 
                                                            i18nExport={t('shared:Export')} */
                                                          i18nPublish={t(
                                                            'shared:Publish'
                                                          )}
                                                          i18nPublished={t(
                                                            'virtualization.publishedDataVirtualization'
                                                          )}
                                                          i18nUnpublish={t(
                                                            'shared:Unpublish'
                                                          )}
                                                          i18nUnpublishModalMessage={t(
                                                            'virtualization.unpublishModalMessage',
                                                            {
                                                              name:
                                                                virtualization.keng__id,
                                                            }
                                                          )}
                                                          i18nUnpublishModalTitle={t(
                                                            'virtualization.unpublishModalTitle'
                                                          )}
                                                          onDelete={
                                                            handleDelete
                                                          }
                                                          /* TD-636: Commented out for TP 
                                                            onExport={
                                                              this
                                                                .handleExportVirtualization
                                                            } */
                                                          onUnpublish={
                                                            handleUnpublish
                                                          }
                                                          onPublish={
                                                            handlePublish
                                                          }
                                                          currentPublishedState={
                                                            publishingDetails.state
                                                          }
                                                          publishingLogUrl={
                                                            publishingDetails.logUrl
                                                          }
                                                          publishingCurrentStep={
                                                            publishingDetails.stepNumber
                                                          }
                                                          publishingTotalSteps={
                                                            publishingDetails.stepTotal
                                                          }
                                                          publishingStepText={
                                                            publishingDetails.stepText
                                                          }
                                                          i18nPublishInProgress={t(
                                                            'virtualization.publishInProgress'
                                                          )}
                                                          i18nUnpublishInProgress={t(
                                                            'virtualization.unpublishInProgress'
                                                          )}
                                                          i18nPublishLogUrlText={t(
                                                            'shared:viewLogs'
                                                          )}
                                                        />
                                                      );
                                                    }
                                                  )}
                                                </VirtualizationList>
                                              )}
                                            </WithLoader>
                                          </PageSection>
                                        );
                                      }}
                                    </WithListViewToolbarHelpers>
                                  </>
                                );
                              }}
                            </WithVirtualizationHelpers>
                          );
                        }}
                      </WithVirtualizations>
                    );
                  }}
                </UIContext.Consumer>
              );
            }}
          </AppContext.Consumer>
        )}
      </Translation>
    );
  }
}
