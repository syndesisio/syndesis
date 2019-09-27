import { useVirtualizations } from '@syndesis/api';
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
import { useTranslation } from 'react-i18next';
import { AppContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../resolvers';
import { VirtualizationHandlers } from '../shared/VirtualizationHandlers';
import {
  getOdataUrl,
  getPublishingDetails,
} from '../shared/VirtualizationUtils';

function getFilteredAndSortedVirtualizations(
  virtualizations: RestDataService[],
  activeFilters: IActiveFilter[],
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

export const VirtualizationsPage: React.FunctionComponent = () => {
  const appContext = React.useContext(AppContext);
  const { t } = useTranslation(['data', 'shared']);
  const {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishVirtualization,
  } = VirtualizationHandlers();
  const { resource: data, hasData, error } = useVirtualizations();

  // TODO: implement handleImportVirt
  // const handleImportVirt = (virtualizationName: string) => {
  //   alert('Import virtualization ' + virtualizationName);
  // }

  /* TD-636: Commented out for TP
  public handleExportVirtualization() {
    // TODO: implement handleExportVirtualization
    alert('Export virtualization ');
  } */

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };

  return appContext.config.datavirt.enabled === 0 ? (
    <SimplePageHeader
      i18nTitle={t('virtualization.virtualizationsPageTitle')}
      i18nDescription={t('virtualization.virtualizationsDisabled')}
    />
  ) : (
    <>
      <SimplePageHeader
        i18nTitle={t('virtualization.virtualizationsPageTitle')}
        i18nDescription={t('virtualization.virtualizationsPageDescription')}
        isTechPreview={true}
        i18nTechPreview={t('shared:techPreview')}
        techPreviewPopoverHtml={
          <span
            dangerouslySetInnerHTML={{
              __html: t('shared:techPreviewPopoverHtml'),
            }}
          />
        }
      />
      <WithListViewToolbarHelpers
        defaultFilterType={filterByName}
        defaultSortType={sortByName}
      >
        {helpers => {
          const filteredAndSorted = getFilteredAndSortedVirtualizations(
            data,
            helpers.activeFilters,
            helpers.isSortAscending
          );
          return (
            <PageSection variant={'light'} noPadding={true}>
              <WithLoader
                error={error !== false}
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
                errorChildren={<ApiError error={error as Error} />}
              >
                {() => (
                  <VirtualizationList
                    filterTypes={filterTypes}
                    sortTypes={sortTypes}
                    resultsCount={filteredAndSorted.length}
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
                    i18nEmptyStateTitle={t('virtualization.emptyStateTitle')}
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
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    linkCreateHRef={resolvers.virtualizations.create()}
                    /* TD-636: Commented out for TP
                      onImport={this.handleImportVirt} */
                    hasListData={data.length > 0}
                  >
                    {filteredAndSorted.map(
                      (virtualization: RestDataService, index: number) => {
                        const publishingDetails = getPublishingDetails(
                          appContext.config.consoleUrl,
                          virtualization
                        );
                        return (
                          <VirtualizationListItem
                            key={index}
                            detailsPageLink={resolvers.virtualizations.views.root(
                              { virtualization }
                            )}
                            hasViews={!virtualization.empty}
                            virtualizationName={virtualization.keng__id}
                            virtualizationDescription={
                              virtualization.tko__description
                                ? virtualization.tko__description
                                : ''
                            }
                            odataUrl={getOdataUrl(virtualization)}
                            i18nCancelText={t('shared:Cancel')}
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteModalMessage={t(
                              'virtualization.deleteModalMessage',
                              {
                                name: virtualization.keng__id,
                              }
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
                            /* TD-636: Commented out for TP
                                i18nExport={t('shared:Export')} */
                            i18nInUseText={getUsedByMessage(virtualization.usedBy)}
                            i18nPublish={t('shared:Publish')}
                            i18nPublished={t(
                              'virtualization.publishedDataVirtualization'
                            )}
                            i18nUnpublish={t('shared:Unpublish')}
                            i18nUnpublishModalMessage={t(
                              'virtualization.unpublishModalMessage',
                              {
                                name: virtualization.keng__id,
                              }
                            )}
                            i18nUnpublishModalTitle={t(
                              'virtualization.unpublishModalTitle'
                            )}
                            onDelete={handleDeleteVirtualization}
                            /* TD-636: Commented out for TP
                                onExport={
                                  this
                                    .handleExportVirtualization
                                } */
                            onUnpublish={handleUnpublishVirtualization}
                            onPublish={handlePublishVirtualization}
                            currentPublishedState={publishingDetails.state}
                            publishingLogUrl={publishingDetails.logUrl}
                            publishingCurrentStep={publishingDetails.stepNumber}
                            publishingTotalSteps={publishingDetails.stepTotal}
                            publishingStepText={publishingDetails.stepText}
                            i18nPublishInProgress={t(
                              'virtualization.publishInProgress'
                            )}
                            i18nUnpublishInProgress={t(
                              'virtualization.unpublishInProgress'
                            )}
                            i18nPublishLogUrlText={t('shared:viewLogs')}
                            usedBy={virtualization.usedBy}
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
};
