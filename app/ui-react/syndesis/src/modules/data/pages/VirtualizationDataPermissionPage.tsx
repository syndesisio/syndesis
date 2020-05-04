import { useViewDefinitionDescriptors, useVirtualization } from '@syndesis/api';
import { ViewDefinitionDescriptor } from '@syndesis/models';
import {
  IFilterType,
  ISortType,
  ViewListSkeleton,
  ViewPermissionList,
  ViewPermissionListItems,
} from '@syndesis/ui';
import {
  useRouteData,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import { VirtualizationActionId } from '../shared/VirtualizationActionContainer';
import { getFilteredAndSortedByName } from '../shared/VirtualizationUtils';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

export const VirtualizationDataPermissionPage: React.FunctionComponent = () => {
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
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Hook to obtain view descriptors.
   */
  const {
    error: viewDefinitionDescriptorsError,
    hasData: hasViewDefinitionDescriptors,
    model: viewDefinitionDescriptors,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  /**
   * React useState Hook to handle state in component.
   */
  const [itemSelected, setItemSelected] = React.useState<string[]>([]);
  const [perPage, setPerPage] = React.useState<number>(20);
  const [page, setPage] = React.useState<number>(1);

  let filteredAndSortedPerPage: ViewDefinitionDescriptor[] = [];

  /**
   * Views selection handling.
   */
  const onSelectedViewChange = (checked: boolean, event: any, view: string) => {
    const itemSelectedCopy = [...itemSelected];
    itemSelectedCopy.push(view);
    setItemSelected(itemSelectedCopy);
  };

  const clearViewSelection = () => {
    setItemSelected([]);
  };

  const selectPageViews = () => {
    const selectedViews: string[] = [];
    for (const view of filteredAndSortedPerPage) {
      selectedViews.push(view.name);
    }
    setItemSelected(selectedViews);
  };

  const selectAllViews = () => {
    const selectedViews: string[] = [];
    for (const view of viewDefinitionDescriptors) {
      selectedViews.push(view.name);
    }
    setItemSelected(selectedViews);
  };

  /**
   * A filter for throwing out views without names.
   * @param view the view being checked
   * @returns `true` if the view has a name
   */
  const filterUndefinedId = (view: ViewDefinitionDescriptor): boolean => {
    return view.name !== undefined;
  };

  const getFilteredAndSortedPerPage = (
    filterList: ViewDefinitionDescriptor[]
  ) => {
    const startCount = perPage * (page - 1);
    const pageViewList = filterList.slice(startCount, startCount + perPage);
    return pageViewList;
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
      <WithListViewToolbarHelpers
        defaultFilterType={filterByName}
        defaultSortType={sortByName}
      >
        {helpers => {
          const filteredAndSorted = getFilteredAndSortedByName(
            viewDefinitionDescriptors,
            helpers.activeFilters,
            helpers.isSortAscending
          );
          filteredAndSortedPerPage = getFilteredAndSortedPerPage(
            filteredAndSorted
          );
          return (
            <>
              <WithLoader
                error={viewDefinitionDescriptorsError !== false}
                loading={
                  virtualization.name === '' || !hasViewDefinitionDescriptors
                }
                loaderChildren={<ViewListSkeleton width={800} />}
                errorChildren={
                  <ApiError error={viewDefinitionDescriptorsError as Error} />
                }
              >
                {() => (
                  <ViewPermissionList
                    filterTypes={filterTypes}
                    sortTypes={sortTypes}
                    resultsCount={filteredAndSorted.length}
                    {...helpers}
                    i18nResultsCount={t('shared:resultsCount', {
                      count: filteredAndSorted.length,
                    })}
                    hasListData={viewDefinitionDescriptors.length > 0}
                    hasViewSelected={itemSelected.length > 0}
                    i18nViewName={t('viewNameDisplay')}
                    i18nPermission={t('permissions')}
                    i18nSelectNone={t('permissionSelectNone')}
                    i18nSelectPage={t('permissionSelectPage', {
                      pageListLenght: filteredAndSortedPerPage.length,
                    })}
                    i18nSelectAll={t('permissionSelectAll', {
                      allListLength: filteredAndSorted.length,
                    })}
                    i18nCancle={t('shared:Cancel')}
                    i18nSave={t('shared:Save')}
                    i18nRead={t('shared:Read')}
                    i18nEdit={t('shared:Edit')}
                    i18nDelete={t('shared:Delete')}
                    i18nAllAccess={t('allAccess')}
                    i18nRole={t('permissionRole')}
                    i18nAddNewRole={t('addNewRole')}
                    i18nSetPermission={t('permissionSet')}
                    i18nClearPermission={t('permissionClear')}
                    i18nSelectedViewsMsg={t('permissionSeletedViews')}
                    i18nSelectedViews={itemSelected.join(', ')}
                    page={page}
                    perPage={perPage}
                    setPerPage={setPerPage}
                    setPage={setPage}
                    clearViewSelection={clearViewSelection}
                    selectAllViews={selectAllViews}
                    selectPageViews={selectPageViews}
                  >
                    {filteredAndSortedPerPage
                      .filter(
                        (viewDefinitionDescriptor: ViewDefinitionDescriptor) =>
                          filterUndefinedId(viewDefinitionDescriptor)
                      )
                      .map(
                        (
                          viewDefinitionDescriptor: ViewDefinitionDescriptor,
                          index: number
                        ) => (
                          <ViewPermissionListItems
                            key={index}
                            i18nRead={t('shared:Read')}
                            i18nEdit={t('shared:Edit')}
                            i18nDelete={t('shared:Delete')}
                            i18nAllAccess={t('allAccess')}
                            i18nRole={t('permissionRole')}
                            i18nAddNewRole={t('addNewRole')}
                            itemSelected={itemSelected}
                            viewId={viewDefinitionDescriptor.id}
                            viewName={viewDefinitionDescriptor.name}
                            onSelectedViewChange={onSelectedViewChange}
                          />
                        )
                      )}
                  </ViewPermissionList>
                )}
              </WithLoader>
            </>
          );
        }}
      </WithListViewToolbarHelpers>
    </VirtualizationEditorPage>
  );
};
