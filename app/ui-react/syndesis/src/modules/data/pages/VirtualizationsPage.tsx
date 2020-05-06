import { useVirtualizations } from '@syndesis/api';
import { Virtualization } from '@syndesis/models';
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
import {
  VirtualizationActionContainer,
} from '../shared/VirtualizationActionContainer';
import {
  getOdataUrl,
  getPublishingDetails,
  getStateLabelStyle,
  getStateLabelText,
  isPublishStep,
} from '../shared/VirtualizationUtils';

function getFilteredAndSortedVirtualizations(
  virtualizations: Virtualization[],
  activeFilters: IActiveFilter[],
  isSortAscending: boolean
) {
  let filteredAndSorted = virtualizations;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter(
      (virtualization: Virtualization) =>
        virtualization.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort(
    (thisVirtualization, thatVirtualization) => {
      if (isSortAscending) {
        return thisVirtualization.name.localeCompare(thatVirtualization.name);
      }

      // sort descending
      return thatVirtualization.name.localeCompare(thisVirtualization.name);
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

const getVirtualizationActions = (virtualization: Virtualization) => {
  const virtualizationActions = (
    <VirtualizationActionContainer
      includeActions={[]}
      virtualization={virtualization}
    />
  );
  return virtualizationActions;
};

export const VirtualizationsPage: React.FunctionComponent = () => {
  const appContext = React.useContext(AppContext);
  const { t } = useTranslation(['data', 'shared']);
  const { resource: data, hasData, error } = useVirtualizations();

  /**
   *
   * @param virtualization the virtualization whose description is being returned
   * @returns the description truncated at 150 chars if necessary
   */
  const getDescription = (virtualization: Virtualization): string => {
    if (virtualization.description) {
      if (virtualization.description.length > 150) {
        return virtualization.description.substring(0, 150) + ' ...';
      }

      return virtualization.description;
    }

    return '';
  };

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };

  return appContext.config.datavirt.enabled === 0 ? (
    <SimplePageHeader
      i18nTitle={t('virtualizationsPageTitle')}
      i18nDescription={t('virtualizationsDisabled')}
    />
  ) : (
    <>
      <SimplePageHeader
        i18nTitle={t('virtualizationsPageTitle')}
        i18nDescription={t('virtualizationsPageDescription')}
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
            <PageSection>
              <WithLoader
                error={error !== false}
                loading={!hasData}
                loaderChildren={<VirtualizationListSkeleton width={800} />}
                errorChildren={<ApiError error={error as Error} />}
              >
                {() => (
                  <VirtualizationList
                    filterTypes={filterTypes}
                    sortTypes={sortTypes}
                    resultsCount={filteredAndSorted.length}
                    {...helpers}
                    i18nCreateDataVirtualization={t('createDataVirtualization')}
                    i18nCreateDataVirtualizationTip={t(
                      'createDataVirtualizationTip'
                    )}
                    i18nEmptyStateInfo={t('emptyStateInfoMessage')}
                    i18nEmptyStateTitle={t('emptyStateTitle')}
                    i18nImport={t('shared:Import')}
                    i18nImportTip={t('importVirtualizationTip')}
                    i18nLinkCreateVirtualization={t('createDataVirtualization')}
                    i18nName={t('shared:Name')}
                    i18nNameFilterPlaceholder={t(
                      'shared:nameFilterPlaceholder'
                    )}
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    linkCreateHRef={resolvers.virtualizations.create()}
                    linkImportHRef={resolvers.virtualizations.import()}
                    hasListData={data.length > 0}
                  >
                    {filteredAndSorted.map(
                      (virtualization: Virtualization, index: number) => {
                        const publishingDetails = getPublishingDetails(
                          appContext.config.consoleUrl,
                          virtualization
                        );
                        const isProgressWithLink = isPublishStep(
                          publishingDetails
                        );
                        const labelType = getStateLabelStyle(publishingDetails);
                        const publishStateText = getStateLabelText(
                          publishingDetails
                        );

                        return (
                          <VirtualizationListItem
                            key={index}
                            isProgressWithLink={isProgressWithLink}
                            i18nPublishState={publishStateText}
                            labelType={labelType}
                            detailsPageLink={resolvers.virtualizations.views.root(
                              { virtualization }
                            )}
                            modified={virtualization.modified}
                            virtualizationName={virtualization.name}
                            virtualizationDescription={getDescription(
                              virtualization
                            )}
                            odataUrl={getOdataUrl(virtualization)}
                            i18nViewODataUrlText={t('viewOData')}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('editDataVirtualizationTip')}
                            i18nLockPopoverHeading={t('dataPermissionPopoverHeading')}
                            i18nLockPopover={t('dataPermissionPopover')}
                            i18nSecuredText={t('dataPermissionSecuredText')}
                            i18nInUseText={getUsedByMessage(
                              virtualization.usedBy
                            )}
                            dropdownActions={getVirtualizationActions(virtualization)}
                            currentPublishedState={publishingDetails.state}
                            currentPublishedVersion={
                              virtualization.publishedRevision
                            }
                            publishingLogUrl={publishingDetails.logUrl}
                            publishingCurrentStep={publishingDetails.stepNumber}
                            publishingTotalSteps={publishingDetails.stepTotal}
                            publishingStepText={publishingDetails.stepText}
                            i18nPublishLogUrlText={t('shared:viewLogs')}
                            usedBy={virtualization.usedBy}
                            secured={virtualization.secured}
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