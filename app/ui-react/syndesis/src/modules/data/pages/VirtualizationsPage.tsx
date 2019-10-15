import { useVirtualizationHelpers, useVirtualizations } from '@syndesis/api';
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
import { AppContext, UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../resolvers';
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

export const VirtualizationsPage: React.FunctionComponent = () => {
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { resource: data, hasData, error } = useVirtualizations();
  const {
    deleteVirtualization,
    exportVirtualization,
    publishVirtualization,
    unpublishVirtualization,
  } = useVirtualizationHelpers();

  const doExport = (virtualizationName: string) => {
    exportVirtualization(virtualizationName).catch((e: any) => {
      // notify user of error
      pushNotification(
        t('exportVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: virtualizationName,
        }),
        'error'
      );
    });
  };

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
            <PageSection variant={'light'} noPadding={true}>
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
                        const doDelete = async (
                          virtId: string
                        ): Promise<string> => {
                          const result = await deleteVirtualization(
                            virtId
                          ).catch((e: any) => {
                            pushNotification(
                              t('deleteVirtualizationFailed', {
                                details: e.errorMessage || e.message || e,
                                name: virtId,
                              }),
                              'error'
                            );
                          });
                          if (result) {
                            return 'DELETED';
                          }
                          return 'FAILED';
                        };
                        const doPublish = async (
                          virtId: string
                        ): Promise<string> => {
                          if (virtualization.empty) {
                            pushNotification(
                              t('publishVirtualizationNoViews', {
                                name: virtId,
                              }),
                              'info'
                            );
                            return 'FAILED';
                          } else {
                            const teiidStatus = await publishVirtualization(
                              virtId
                            ).catch((e: any) => {
                              pushNotification(
                                t('publishVirtualizationFailed', {
                                  details: e.errorMessage || e.message || error,
                                  name: virtId,
                                }),
                                'error'
                              );
                            });
                            if (teiidStatus) {
                              if (teiidStatus.attributes['Build Status']) {
                                return teiidStatus.attributes['Build Status'];
                              }
                              return 'SUBMITTED';
                            }
                            return 'FAILED';
                          }
                        };
                        const doUnpublish = async (
                          virtId: string
                        ): Promise<string> => {
                          const buildStatus = await unpublishVirtualization(
                            virtId
                          ).catch((e: any) => {
                            if (e.name === 'AlreadyUnpublished') {
                              pushNotification(
                                t('unpublishedVirtualization', {
                                  name: virtId,
                                }),
                                'info'
                              );
                            } else {
                              pushNotification(
                                t('unpublishVirtualizationFailed', {
                                  details: e.errorMessage || e.message || error,
                                  name: virtId,
                                }),
                                'error'
                              );
                            }
                          });
                          if (buildStatus) {
                            if (buildStatus.status) {
                              return buildStatus.status;
                            }
                            return 'DELETE_SUBMITTED';
                          }
                          return 'FAILED';
                        };
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
                            i18nDeleteInProgressText={t('deleteInProgress')}
                            i18nPublishInProgressText={t('publishInProgress')}
                            i18nUnpublishInProgressText={t(
                              'unpublishInProgress'
                            )}
                            i18nPublishState={publishStateText}
                            labelType={labelType}
                            detailsPageLink={resolvers.virtualizations.views.root(
                              { virtualization }
                            )}
                            hasViews={!virtualization.empty}
                            virtualizationName={virtualization.name}
                            virtualizationDescription={getDescription(
                              virtualization
                            )}
                            odataUrl={getOdataUrl(virtualization)}
                            i18nCancelText={t('shared:Cancel')}
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteModalMessage={t('deleteModalMessage', {
                              name: virtualization.name,
                            })}
                            i18nDeleteModalTitle={t('deleteModalTitle')}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('editDataVirtualizationTip')}
                            i18nError={t('shared:Error')}
                            i18nExport={t('shared:Export')}
                            i18nInUseText={getUsedByMessage(
                              virtualization.usedBy
                            )}
                            i18nPublish={t('shared:Publish')}
                            i18nUnpublish={t('shared:Unpublish')}
                            i18nUnpublishModalMessage={t(
                              'unpublishModalMessage',
                              {
                                name: virtualization.name,
                              }
                            )}
                            i18nUnpublishModalTitle={t('unpublishModalTitle')}
                            onDelete={doDelete}
                            onExport={doExport}
                            onUnpublish={doUnpublish}
                            onPublish={doPublish}
                            currentPublishedState={publishingDetails.state}
                            publishingLogUrl={publishingDetails.logUrl}
                            publishingCurrentStep={publishingDetails.stepNumber}
                            publishingTotalSteps={publishingDetails.stepTotal}
                            publishingStepText={publishingDetails.stepText}
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
