import {
  usePolling,
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import {
  ViewDefinitionDescriptor,
  VirtualizationPublishingDetails,
} from '@syndesis/models';
import { Virtualization } from '@syndesis/models';
import {
  Loader,
  PageSection,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { VirtualizationNavBar } from '../shared';
import {
  getOdataUrl,
  getPublishingDetails,
  getStateLabelStyle,
  getStateLabelText,
  isPublishStep,
} from '../shared/VirtualizationUtils';

import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewList,
  ViewListItem,
  ViewListSkeleton,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';
import './VirtualizationViewsPage.css';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationViewsPageRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationViewsPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationViewsPageRouteState {
  virtualization: Virtualization;
}

function getFilteredAndSortedViewDefns(
  viewDefinitionDescriptors: ViewDefinitionDescriptor[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSorted = viewDefinitionDescriptors;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSorted = filteredAndSorted.filter(
      (view: ViewDefinitionDescriptor) =>
        view.name.toLowerCase().includes(valueToLower)
    );
  });

  filteredAndSorted = filteredAndSorted.sort((thisView, thatView) => {
    if (isSortAscending) {
      return thisView.name.localeCompare(thatView.name);
    }

    // sort descending
    return thatView.name.localeCompare(thisView.name);
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

export const VirtualizationViewsPage: React.FunctionComponent = () => {
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<
    IVirtualizationViewsPageRouteParams,
    IVirtualizationViewsPageRouteState
  >();
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );

  const [description, setDescription] = React.useState(
    state.virtualization.description
  );
  const [currPublishedState, setCurrPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [isProgressWithLink, setProgressWithLink] = React.useState(false);
  const [isSubmitted, setSubmitted] = React.useState(false);
  const [labelType, setLabelType] = React.useState('default' as 'danger' | 'primary' | 'default');
  const [publishStateText, setPublishStateText] = React.useState();
  const [usedBy, setUsedBy] = React.useState(state.virtualization.usedBy);

  const {
    deleteViewDefinition,
    deleteVirtualization,
    exportVirtualization,
    publishVirtualization,
    unpublishVirtualization,
    updateVirtualizationDescription,
  } = useVirtualizationHelpers();

  const filterUndefinedId = (view: ViewDefinitionDescriptor): boolean => {
    return view.name !== undefined;
  };

  const {
    resource: viewDefinitionDescriptors,
    hasData: hasViewDefinitionDescriptors,
    error: viewDefinitionDescriptorsError,
    read,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  const updatePublishedState = async () => {
    const publishedDetails: VirtualizationPublishingDetails = getPublishingDetails(
      appContext.config.consoleUrl,
      virtualization
    ) as VirtualizationPublishingDetails;

    setCurrPublishedState(publishedDetails);
    setUsedBy(virtualization.usedBy);
};

  // poll to check for updates to the published state
  usePolling({ callback: updatePublishedState, delay: 5000 });

  React.useEffect(() => {
    // turn off once publish/unpublish shows in-progress
    if (
      currPublishedState.state === 'DELETE_SUBMITTED' ||
      currPublishedState.state === 'SUBMITTED' ||
      isProgressWithLink
    ) {
      setSubmitted(false);
    }

    setProgressWithLink(isPublishStep(currPublishedState));

    if (!isSubmitted) {
      setLabelType(getStateLabelStyle(currPublishedState));
      setPublishStateText(getStateLabelText(currPublishedState));
    }
  }, [currPublishedState, isProgressWithLink, isSubmitted]);

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };

  const doDelete = async (virtId: string): Promise<void> => {
    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('deleteInProgress'));
    await deleteVirtualization(virtId).catch((e: any) => {
      pushNotification(
        t('deleteVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: virtId,
        }),
        'error'
      );

      // restore previous values
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
      setSubmitted(false);
      throw e;
    });

    // successfully deleted navigate to the virtualizations list page
    history.push(resolvers.data.virtualizations.list());
  };

  const doExport = () => {
    exportVirtualization(virtualization.name).catch((e: any) => {
      // notify user of error
      pushNotification(
        t('exportVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: virtualization.name,
        }),
        'error'
      );
    });
  }

  const doPublish = async (
    virtId: string,
    hasViews: boolean
  ) => {
    if (!hasViews) {
      pushNotification(
        t('publishVirtualizationNoViews', {
          name: virtId,
        }),
        'info'
      );
      const e = new Error();
      e.name = 'NoViews';
      throw e;
    }
    
    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('publishInProgress'));
    await publishVirtualization(virtId).catch(
      (e: any) => {
        pushNotification(
          t('publishVirtualizationFailed', {
            details: e.errorMessage || e.message || e,
            name: virtId,
          }),
          'error'
        );
        setPublishStateText(saveText);
        setLabelType(saveLabelType);
        setSubmitted(false);
        throw e;
      }
    );
  };

  const doUnpublish = async (virtId: string): Promise<void> => {
    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('unpublishInProgress'));
    await unpublishVirtualization(virtId).catch(
      (e: any) => {
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
              details: e.errorMessage || e.message || e,
              name: virtId,
            }),
            'error'
          );
        }

        // restore previous state
        setPublishStateText(saveText);
        setLabelType(saveLabelType);
        setSubmitted(false);
        throw e;
      }
    );
  };

  const doSetDescription = async (newDescription: string) => {
    const previous = description;
    setDescription(newDescription); // this sets InlineTextEdit component to new value
    try {
      await updateVirtualizationDescription(
        params.virtualizationId,
        newDescription
      );
      virtualization.description = newDescription;
      return true;
    } catch {
      pushNotification(
        t('errorUpdatingDescription', {
          name: state.virtualization.name,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
  };

  const handleDeleteView = async (viewId: string, viewName: string) => {
    // Delete the view
    try {
      await deleteViewDefinition(viewId);

      pushNotification(
        t('deleteViewSuccess', {
          name: viewName,
        }),
        'success'
      );

      await read();
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('deleteViewFailed', {
          details,
          name: viewName,
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
        const filteredAndSorted = getFilteredAndSortedViewDefns(
          viewDefinitionDescriptors,
          helpers.activeFilters,
          helpers.currentSortType,
          helpers.isSortAscending
        );
        return (
          <>
            <PageSection variant={'light'} noPadding={true}>
              <ViewHeaderBreadcrumb
                isSubmitted={isSubmitted}
                currentPublishedState={currPublishedState.state}
                virtualizationName={state.virtualization.name}
                dashboardHref={resolvers.dashboard.root()}
                dashboardString={t('shared:Home')}
                dataHref={resolvers.data.root()}
                dataString={t('shared:Virtualizations')}
                i18nCancelText={t('shared:Cancel')}
                i18nDelete={t('shared:Delete')}
                i18nDeleteModalMessage={t('deleteModalMessage', {
                  name: state.virtualization.name,
                })}
                i18nDeleteModalTitle={t('deleteModalTitle')}
                i18nExport={t('shared:Export')}
                i18nPublish={t('shared:Publish')}
                i18nUnpublish={t('shared:Unpublish')}
                i18nUnpublishModalMessage={t('unpublishModalMessage', {
                  name: state.virtualization.name,
                })}
                i18nUnpublishModalTitle={t('unpublishModalTitle')}
                onDelete={doDelete}
                onExport={doExport}
                onUnpublish={doUnpublish}
                onPublish={doPublish}
                hasViews={viewDefinitionDescriptors.length > 0}
                usedInIntegration={usedBy.length > 0}
              />
            </PageSection>
            <PageSection
              className={'virtualization-views-page'}
              variant={'light'}
              noPadding={true}
            >
              {virtualization ? (
                <VirtualizationDetailsHeader
                  isProgressWithLink={isProgressWithLink}
                  i18nPublishState={publishStateText}
                  labelType={labelType}
                  i18nDescriptionPlaceholder={t('descriptionPlaceholder')}
                  i18nInUseText={getUsedByMessage(usedBy)}
                  i18nPublishLogUrlText={t('shared:viewLogs')}
                  odataUrl={getOdataUrl(virtualization)}
                  publishedState={currPublishedState.state}
                  publishingCurrentStep={currPublishedState.stepNumber}
                  publishingLogUrl={currPublishedState.logUrl}
                  publishingTotalSteps={currPublishedState.stepTotal}
                  publishingStepText={currPublishedState.stepText}
                  virtualizationDescription={description}
                  virtualizationName={state.virtualization.name}
                  isWorking={false}
                  onChangeDescription={doSetDescription}
                />
              ) : (
                <Loader size={'sm'} inline={true} />
              )}
            </PageSection>
            <PageSection variant={'light'} noPadding={true}>
              <VirtualizationNavBar virtualization={virtualization} />
            </PageSection>
            <PageSection variant={'light'} noPadding={true}>
              <WithLoader
                error={viewDefinitionDescriptorsError !== false}
                loading={!hasViewDefinitionDescriptors}
                loaderChildren={<ViewListSkeleton width={800} />}
                errorChildren={
                  <ApiError error={viewDefinitionDescriptorsError as Error} />
                }
              >
                {() => (
                  <ViewList
                    filterTypes={filterTypes}
                    sortTypes={sortTypes}
                    resultsCount={filteredAndSorted.length}
                    {...helpers}
                    i18nEmptyStateInfo={t('viewEmptyStateInfo')}
                    i18nEmptyStateTitle={t('viewEmptyStateTitle')}
                    i18nImportViews={t('importDataSource')}
                    i18nImportViewsTip={t('importDataSourceTip')}
                    i18nCreateView={t('createView')}
                    i18nCreateViewTip={t('createViewTip')}
                    i18nName={t('shared:Name')}
                    i18nNameFilterPlaceholder={t(
                      'shared:nameFilterPlaceholder'
                    )}
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    linkCreateViewHRef={resolvers.data.virtualizations.views.createView.selectSources(
                      {
                        virtualization,
                      }
                    )}
                    linkImportViewsHRef={resolvers.data.virtualizations.views.importSource.selectConnection(
                      {
                        virtualization,
                      }
                    )}
                    hasListData={viewDefinitionDescriptors.length > 0}
                  >
                    {filteredAndSorted
                      .filter(
                        (viewDefinitionDescriptor: ViewDefinitionDescriptor) =>
                          filterUndefinedId(viewDefinitionDescriptor)
                      )
                      .map(
                        (
                          viewDefinitionDescriptor: ViewDefinitionDescriptor,
                          index: number
                        ) => (
                          <ViewListItem
                            key={index}
                            viewId={viewDefinitionDescriptor.id}
                            viewName={viewDefinitionDescriptor.name}
                            viewDescription={
                              viewDefinitionDescriptor.description
                            }
                            viewEditPageLink={resolvers.data.virtualizations.views.edit.sql(
                              {
                                virtualization,
                                // tslint:disable-next-line: object-literal-sort-keys
                                viewDefinitionId: viewDefinitionDescriptor.id,
                                viewDefinition: undefined,
                              }
                            )}
                            i18nCancelText={t('shared:Cancel')}
                            i18nDelete={t('shared:Delete')}
                            i18nDeleteModalMessage={t(
                              'deleteViewModalMessage',
                              {
                                name: viewDefinitionDescriptor.name,
                              }
                            )}
                            i18nDeleteModalTitle={t('deleteModalTitle')}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('shared:Edit')}
                            onDelete={handleDeleteView}
                          />
                        )
                      )}
                  </ViewList>
                )}
              </WithLoader>
            </PageSection>
          </>
        );
      }}
    </WithListViewToolbarHelpers>
  );
};
