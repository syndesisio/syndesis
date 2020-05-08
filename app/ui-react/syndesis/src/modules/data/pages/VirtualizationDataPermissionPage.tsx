import {
  useDVRoles,
  useDVStatus,
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { RoleInfo, ViewDefinitionDescriptor } from '@syndesis/models';
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
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import { VirtualizationActionId } from '../shared/VirtualizationActionContainer';
import { getFilteredAndSortedByName } from '../shared/VirtualizationUtils';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

export const VirtualizationDataPermissionPage: React.FunctionComponent = () => {
  /**
   * Context that broadcasts global notifications.
   */
  const { pushNotification } = React.useContext(UIContext);

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);

  /**
   * Hook that provides helper methods.
   */
  const { updateVirtualizationRoles } = useVirtualizationHelpers();

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
   * Hook to obtain the dv status is sso configured
   */
  const { resource: dvStatus, read: getDVStatusUpdate } = useDVStatus();

  /**
   * Hook to obtain the avalable roles.
   */
  const { resource: dvRoles, read: getUpdatedRole } = useDVRoles();

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
  const [itemSelected, setItemSelected] = React.useState<Map<string, string>>(
    new Map<string, string>()
  );
  const [perPage, setPerPage] = React.useState<number>(20);
  const [page, setPage] = React.useState<number>(1);

  let filteredAndSortedPerPage: ViewDefinitionDescriptor[] = [];

  /**
   * Views selection handling.
   */
  const onSelectedViewChange = (
    checked: boolean,
    event: any,
    viewName: string,
    viewId: string
  ) => {
    const itemSelectedCopy = new Map(itemSelected);
    if (checked) {
      itemSelectedCopy.set(viewId, viewName);
    } else {
      itemSelectedCopy.delete(viewId);
    }

    setItemSelected(itemSelectedCopy);
  };

  const clearViewSelection = () => {
    setItemSelected(new Map<string, string>());
  };

  const selectPageViews = () => {
    const selectedViews: Map<string, string> = new Map<string, string>();
    for (const view of filteredAndSortedPerPage) {
      selectedViews.set(view.id, view.name);
    }
    setItemSelected(selectedViews);
  };

  const selectAllViews = () => {
    const selectedViews: Map<string, string> = new Map<string, string>();
    for (const view of viewDefinitionDescriptors) {
      selectedViews.set(view.id, view.name);
    }
    setItemSelected(selectedViews);
  };

  const updateViewsPermissions = async (roleInfo: RoleInfo) => {
    try {
      await updateVirtualizationRoles(params.virtualizationId, roleInfo);
      return true;
    } catch {
      pushNotification(
        t('errorUpdatingViewPermissions', {
          name: params.virtualizationId,
        }),
        'error'
      );
      return false;
    }
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
                    hasViewSelected={itemSelected.size > 0}
                    i18nViewName={t('viewNameDisplay')}
                    i18nPermission={t('permissions')}
                    i18nSelectNone={t('permissionSelectNone')}
                    i18nSelectPage={t('permissionSelectPage', {
                      pageListLenght: filteredAndSortedPerPage.length,
                    })}
                    i18nSelectAll={t('permissionSelectAll', {
                      allListLength: filteredAndSorted.length,
                    })}
                    i18nEmptyStateInfo={t('viewEmptyStateInfo')}
                    i18nEmptyStateTitle={t('viewEmptyStateTitle')}
                    i18nImportViews={t('importViews')}
                    i18nImportViewsTip={t('importDataSourceTip')}
                    i18nCreateView={t('createView')}
                    i18nCreateViewTip={t('createViewTip')}
                    i18nCancle={t('shared:Cancel')}
                    i18nSave={t('shared:Save')}
                    i18nSelect={t('shared:Select')}
                    i18nInsert={t('shared:Insert')}
                    i18nUpdate={t('shared:Update')}
                    i18nDelete={t('shared:Delete')}
                    i18nAllAccess={t('allAccess')}
                    i18nRole={t('permissionRole')}
                    i18nAddNewRole={t('addNewRole')}
                    i18nSetPermission={t('permissionSet')}
                    i18nClearPermission={t('permissionClear')}
                    i18nSelectedViewsMsg={t('permissionSeletedViews')}
                    i18nSsoConfigWarning={t('permissionSsoConfig')}
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
                    page={page}
                    perPage={perPage}
                    setPerPage={setPerPage}
                    setPage={setPage}
                    clearViewSelection={clearViewSelection}
                    selectAllViews={selectAllViews}
                    selectPageViews={selectPageViews}
                    status={dvStatus.attributes}
                    dvRoles={dvRoles}
                    itemSelected={itemSelected}
                    updateViewsPermissions={updateViewsPermissions}
                    getDVStatusUpdate={getDVStatusUpdate}
                    getUpdatedRole={getUpdatedRole}
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
                            // i18nSelect={t('shared:Select')}
                            // i18nInsert={t('shared:Insert')}
                            // i18nUpdate={t('shared:Update')}
                            // i18nDelete={t('shared:Delete')}
                            // i18nAllAccess={t('allAccess')}
                            // i18nRole={t('permissionRole')}
                            // i18nAddNewRole={t('addNewRole')}
                            itemSelected={itemSelected}
                            viewId={viewDefinitionDescriptor.id}
                            viewName={viewDefinitionDescriptor.name}
                            viewRolePermissionList={
                              viewDefinitionDescriptor.tablePrivileges
                            }
                            // status={dvStatus.attributes}
                            onSelectedViewChange={onSelectedViewChange}
                            // dvRoles={dvRoles}
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
