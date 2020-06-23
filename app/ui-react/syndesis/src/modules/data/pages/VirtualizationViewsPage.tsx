import {
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { ViewDefinitionDescriptor } from '@syndesis/models';
import {
  IActiveFilter,
  IFilterType,
  ISortType,
  ViewList,
  ViewListItem,
  ViewListSkeleton,
} from '@syndesis/ui';
import {
  useRouteData,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError, PageTitle } from '../../../shared';
import resolvers from '../../resolvers';
import {
  VirtualizationActionId,
} from '../shared/VirtualizationActionContainer';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

export const VirtualizationViewsPage: React.FunctionComponent = () => {
  const getFilteredAndSortedViewDefns = (
    viewDescriptors: ViewDefinitionDescriptor[],
    activeFilters: IActiveFilter[],
    isSortAscending: boolean
  ) => {
    let filteredAndSorted = viewDescriptors;
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
  };

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

  /**
   * Context that broadcasts global notifications.
   */
  const { pushNotification } = React.useContext(UIContext);

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);

  /**
   * Hook to obtain route params and history.
   */
  const { params, state } = useRouteData<
    IVirtualizationEditorPageRouteParams,
    IVirtualizationEditorPageRouteState
  >();

  /**
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Hook to obtain view descriptors. Also does polling to get any view descriptor updates.
   */
  const {
    error: viewDefinitionDescriptorsError,
    hasData: hasViewDefinitionDescriptors,
    model: viewDefinitionDescriptors,
    read,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  /**
   * Hook that provides helper methods.
   */
  const { deleteViewDefinition } = useVirtualizationHelpers();

  /**
   * A filter for throwing out views without names.
   * @param view the view being checked
   * @returns `true` if the view has a name
   */
  const filterUndefinedId = (view: ViewDefinitionDescriptor): boolean => {
    return view.name !== undefined;
  };

  /**
   * Callback that deletes the specified view.
   * @param viewId the ID of the view being deleted
   * @param viewName the name of the view
   */
  const handleDeleteView = async (viewId: string, viewName: string) => {
    try {
      await deleteViewDefinition(viewId);
      // inform user view has been deleted
      pushNotification(
        t('deleteViewSuccess', {
          name: viewName,
        }),
        'success'
      );

      await read(); // update list of views
    } catch (error) {
      const details = error.message ? error.message : '';
      // inform user of error
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
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
      items={[VirtualizationActionId.Stop, VirtualizationActionId.Delete]}
      actions={[VirtualizationActionId.Publish]}
      publishActionCustomProps={{ as: 'default' }}
    >
      <PageTitle title={t('viewsPageTitle')} />
      <WithListViewToolbarHelpers
        defaultFilterType={filterByName}
        defaultSortType={sortByName}
      >
        {helpers => {
          const filteredAndSorted = getFilteredAndSortedViewDefns(
            viewDefinitionDescriptors,
            helpers.activeFilters,
            helpers.isSortAscending
          );
          return (
            <>
              <WithLoader
                error={viewDefinitionDescriptorsError !== false}
                loading={virtualization.name === "" || !hasViewDefinitionDescriptors}
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
                    i18nImportViews={t('importViews')}
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
                            i18nDeleteModalTitle={t('deleteViewModalTitle')}
                            i18nEdit={t('shared:Edit')}
                            i18nEditTip={t('shared:Edit')}
                            i18nInvalid={t('Invalid')}
                            isValid={viewDefinitionDescriptor.valid}
                            onDelete={handleDeleteView}
                          />
                        )
                      )}
                  </ViewList>
                )}
              </WithLoader>
            </>
          );
        }}
      </WithListViewToolbarHelpers>
    </VirtualizationEditorPage>
  );
};
