import { useViewDefinition, useVirtualization, useVirtualizationHelpers} from '@syndesis/api';
import {
  QueryResults,
  RestDataService,
  ViewDefinition,
} from '@syndesis/models';
import {
  Breadcrumb,
  ExpandablePreview,
  PageLoader,
  PageSection,
  PreviewButtonSelection,
  ViewOutputToolbar,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../../app';
import { ApiError } from '../../../../shared';
import resolvers from '../../../resolvers';
import { ViewEditorNavBar } from '../../shared';
import { getPreviewSql, getQueryColumns, getQueryRows } from '../../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization that the view belongs to
 * @param viewDefinitionId - the id of the view definition being edited
 */
export interface IViewEditorOutputRouteParams {
  virtualizationId: string;
  viewDefinitionId: string;
}

/**
 * @param previewExpanded - expanded state of the preview area
 * @param viewDefinition - the ViewDefinition
 * @param previewExpanded - the state of preview component expansion
 * @param previewButtonSelection - the button selection state in the preview component
 * @param queryResults - the current query results in the preview component
 */
export interface IViewEditorOutputRouteState {
  virtualization: RestDataService;
  previewExpanded: boolean;
  viewDefinition: ViewDefinition;
  previewButtonSelection: PreviewButtonSelection;
  queryResults: QueryResults;
}

export const ViewEditorOutputPage: React.FunctionComponent = () => {
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<IViewEditorOutputRouteParams, IViewEditorOutputRouteState>();

  const [activeFilter, setActiveFilter] = React.useState();
  const [columnsToDelete] = React.useState();
  const [enableRemoveColumn] = React.useState(false);
  const [enableReorderColumnDown] = React.useState(false);
  const [enableReorderColumnUp] = React.useState(false);
  const [enableSave] = React.useState(false);
  const [filterResultsMessage, setFilterResultsMessage] = React.useState();
  const [previewExpanded, setPreviewExpanded] = React.useState(
    state.previewExpanded
  );
  const [previewButtonSelection, setPreviewButtonSelection] = React.useState(state.previewButtonSelection);
  // const [viewDdl, setViewDdl] = React.useState(state.viewDefinition.ddl);
  const [queryResults, setQueryResults] = React.useState(state.queryResults);
  const { queryVirtualization } = useVirtualizationHelpers();
  const { resource: virtualization } = useVirtualization(params.virtualizationId);
  const { resource: viewDefn, loading, error } = useViewDefinition(params.viewDefinitionId, state.viewDefinition);

  const handleActiveFilterClosed = () => {
    setActiveFilter(null);
    setFilterResultsMessage(null);
  };

  const handleAddColumn = () => {
    // TODO: implement add column
  };

  const handleApplyFilter = (filterBy: string, filter: string) => {
    setFilterResultsMessage('blah'); // TODO: construct message
    setActiveFilter('blah'); // TODO: construct active filter
  };

  const handleCancel = () => {
    // TODO: implement cancel
  };

  const handlePreviewExpandedChanged = (expanded: boolean) => {
    setPreviewExpanded(expanded);
  };

  const handleRemoveColumn = () => {
    // TODO: implement remove column
  };

  const handleReorderDown = () => {
    // TODO: implement reorder down
  };

  const handleReorderUp = () => {
    // TODO: implement reorder up
  };

  const handleSave = () => {
    // TODO: implement save
  };

  const handlePreviewButtonSelectionChanged = (
    selection: PreviewButtonSelection
  ) => {
    setPreviewButtonSelection(selection);
  };
  
  const handleEditFinished = () => {
    history.push(
      resolvers.data.virtualizations.views.root({
        virtualization: state.virtualization,
      })
    );
  }
  
  const handleRefreshResults = async () => {
    try {
      let sqlStatement = '';
      if (state.viewDefinition) {
        sqlStatement = getPreviewSql(state.viewDefinition);
      }
      const results: QueryResults = await queryVirtualization(
        params.virtualizationId,
        sqlStatement,
        15,
        0
      );
      pushNotification(
        t('virtualization.queryViewSuccess', {
          name: state.viewDefinition.name,
        }),
        'success'
      );
      setQueryResults(results);
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.queryViewFailed', {
          details,
          name: state.viewDefinition.name,
        }),
        'error'
      );
    }
  };
  
  return (
    <WithLoader
      loading={loading}
      loaderChildren={<PageLoader />}
      error={error !== false}
      errorChildren={<ApiError error={error as Error} />}
    >
      {() => (
        <>
          <Breadcrumb>
            <Link to={resolvers.dashboard.root()}>
              {t('shared:Home')}
            </Link>
            <Link to={resolvers.data.root()}>
              {t('shared:DataVirtualizations')}
            </Link>
            <Link
              to={resolvers.data.virtualizations.views.root(
                {
                  virtualization,
                }
              )}
            >
              {params.virtualizationId}
            </Link>
            <span>{viewDefn.name}</span>
          </Breadcrumb>
          <PageSection variant={'light'} noPadding={true}>
            <ViewEditorNavBar
              virtualization={virtualization}
              viewDefinitionId={params.viewDefinitionId}
              viewDefinition={viewDefn}
              previewExpanded={previewExpanded}
              previewButtonSelection={previewButtonSelection} 
              queryResults={queryResults}
              onEditFinished={handleEditFinished}            />
          </PageSection>
          <PageSection>
            <ViewOutputToolbar
              a11yFilterColumns={t('virtualization.viewEditor.applyColumnFilter')}
              a11yFilterText={t(
                'virtualization.viewEditor.columnFilterSearchString'
              )}
              a11yReorderDown={t('virtualization.viewEditor.reorderColumnDown')}
              a11yReorderUp={t('virtualization.viewEditor.reorderColumnUp')}
              activeFilter={activeFilter}
              columnsToDelete={columnsToDelete}
              enableAddColumn={true}
              enableRemoveColumn={enableRemoveColumn}
              enableReorderColumnDown={enableReorderColumnDown}
              enableReorderColumnUp={enableReorderColumnUp}
              enableSave={enableSave}
              i18nActiveFilter={t('virtualization.viewEditor.ActiveFilter')}
              i18nAddColumn={t('virtualization.viewEditor.AddColumn')}
              i18nCancel={t('shared:Cancel')}
              i18nFilterPlaceholder={t('shared:filterByNamePlaceholder')}
              i18nFilterResultsMessage={filterResultsMessage}
              i18nFilterValues={[t('shared:Name')]}
              i18nRemove={t('shared:Remove')}
              i18nRemoveColumn={t('virtualization.viewEditor.RemoveColumn')}
              i18nRemoveColumnDialogConfirmMessage={t(
                'virtualization.viewEditor.removeColumnDialogConfirmMessage'
              )}
              i18nRemoveColumnDialogHeader={t(
                'virtualization.viewEditor.removeColumnDialogHeader'
              )}
              i18nRemoveColumnDialogMessage={t(
                'virtualization.viewEditor.removeColumnDialogMessage'
              )}
              i18nSave={t('shared:Save')}
              onActiveFilterClosed={handleActiveFilterClosed}
              onAddColumn={handleAddColumn}
              onCancel={handleCancel}
              onFilter={handleApplyFilter}
              onRemoveColumn={handleRemoveColumn}
              onReorderColumnDown={handleReorderDown}
              onReorderColumnUp={handleReorderUp}
              onSave={handleSave}
            />
          </PageSection>
          <PageSection variant={'light'} noPadding={true}>
            <ExpandablePreview
              i18nEmptyResultsTitle={t(
                'data:virtualization.preview.resultsTableEmptyStateTitle'
              )}
              i18nEmptyResultsMsg={t(
                'data:virtualization.preview.resultsTableEmptyStateInfo'
              )}
              i18nHidePreview={t('data:virtualization.preview.hidePreview')}
              i18nShowPreview={t('data:virtualization.preview.showPreview')}
              i18nSelectSqlText={t('data:virtualization.preview.selectSql')}
              i18nSelectPreviewText={t('data:virtualization.preview.selectPreview')}
              initialExpanded={previewExpanded}
              initialPreviewButtonSelection={previewButtonSelection}
              onPreviewExpandedChanged={handlePreviewExpandedChanged}
              onPreviewButtonSelectionChanged={handlePreviewButtonSelectionChanged}
              onRefreshResults={handleRefreshResults}
              viewDdl={state.viewDefinition.ddl}
              queryResultRows={getQueryRows(
                queryResults
              )}
              queryResultCols={getQueryColumns(
                queryResults
              )}
            />
          </PageSection>
        </>
      )}
    </WithLoader>
  );
};
