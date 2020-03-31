import {
  ApiContext,
  useViewDefinition,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { TableColumns } from '@syndesis/models';
import {
  QueryResults,
  ViewDefinition,
  ViewSourceInfo,
  Virtualization,
} from '@syndesis/models';
import { Breadcrumb, DdlEditor, IViewEditValidationResult } from '@syndesis/ui';
import { ExpandablePreview, PageLoader } from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../../app';
import { ApiError } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import resolvers from '../../../resolvers';
import {
  generateTableColumns,
  getPreviewSql,
  getQueryColumns,
  getQueryRows,
} from '../../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization that the view belongs to
 * @param viewDefinitionId - the name of the view being edited
 */
export interface IViewEditorSqlRouteParams {
  virtualizationId: string;
  viewDefinitionId: string;
}

/**
 * @param virtualization - the Virtualization
 * @param viewDefinition - the ViewDefinition
 */
export interface IViewEditorSqlRouteState {
  virtualization: Virtualization;
  viewDefinition: ViewDefinition;
}

export const ViewEditorSqlPage: React.FunctionComponent = () => {
  const queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };
  const [isSaving, setIsSaving] = React.useState(false);
  const [isLoadingPreview, setIsLoadingPreview] = React.useState(false);
  const [isMetadataLoaded, setMetadataLoaded] = React.useState(false);
  const [isQueryResultsLoaded, setQueryResultsLoaded] = React.useState(false);
  const [sourceTableColumns, setSourceTableColumns] = React.useState<
    TableColumns[]
  >([]);
  const [sourceInfo, setSourceInfo] = React.useState<any>([]);
  const [viewValid, setViewValid] = React.useState(true);
  const [
    validationMessageVisible,
    setValidationMessageVisible,
  ] = React.useState(false);
  const [validationResults, setValidationResults] = React.useState<
    IViewEditValidationResult[]
  >([]);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const [validationResultsTitle, setValidationResultsTitle] = React.useState(
    t('validationResultsTitle')
  );
  const { params, state, history } = useRouteData<
    IViewEditorSqlRouteParams,
    IViewEditorSqlRouteState
  >();
  const {
    getSourceInfoForView,
    queryVirtualization,
    saveViewDefinition,
  } = useVirtualizationHelpers();
  const [previewExpanded, setPreviewExpanded] = React.useState(true);
  const [queryResults, setQueryResults] = React.useState(queryResultsEmpty);
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );
  const { resource: viewDefn, loading, error } = useViewDefinition(
    params.viewDefinitionId,
    state.viewDefinition
  );
  const [viewVersion, setViewVersion] = React.useState(viewDefn.version);
  const [noResultsTitle, setNoResultsTitle] = React.useState(
    t('preview.resultsTableValidEmptyTitle')
  );
  const [noResultsMessage, setNoResultsMessage] = React.useState(
    t('preview.resultsTableValidEmptyInfo')
  );
  const ddlHasChanges = React.useRef(false);

  const handleMetadataLoaded = async (): Promise<void> => {
    if (sourceTableColumns != null && sourceTableColumns.length > 0) {
      setMetadataLoaded(true);
    }
  };

  const handleQueryResultsLoaded = async (): Promise<void> => {
    setQueryResultsLoaded(true);
  };

  /**
   * Update the view status and results
   * @param view the view definition
   */
  const updateViewStatusAndResults = (view: ViewDefinition) => {
    const isValid = view.status === 'SUCCESS';
    let validationResult = null;
    if (isValid) {
      validationResult = {
        message: '',
        type: 'success',
      } as IViewEditValidationResult;
      // Update no results title and message
      setNoResultsTitle(t('preview.resultsTableValidEmptyTitle'));
      setNoResultsMessage(t('preview.resultsTableValidEmptyInfo'));
    } else {
      validationResult = {
        message: view.message ? view.message : 'Validation Error',
        type: 'danger',
      } as IViewEditValidationResult;
      // Update no results title and message
      setNoResultsTitle(t('preview.resultsTableInvalidEmptyTitle'));
      setNoResultsMessage(t('preview.resultsTableInvalidEmptyInfo'));
    }
    setValidationResultsTitle(t('validationResultsTitle'));
    setValidationResults([validationResult]);
    setValidationMessageVisible(!isValid);
    setViewValid(isValid);
    updateQueryResults(isValid);
  };

  /**
   * Saves View with the new DDL value.  View validation state and results are updated
   */
  const handleSaveView = async (ddlValue: string): Promise<boolean> => {
    setIsSaving(true);

    // View Definition
    const view: ViewDefinition = {
      complete: viewDefn.complete,
      dataVirtualizationName: viewDefn.dataVirtualizationName,
      ddl: ddlValue,
      description: viewDefn.description,
      id: viewDefn.id,
      message: '',
      name: viewDefn.name,
      sourcePaths: viewDefn.sourcePaths,
      status: 'ERROR',
      userDefined: true,
      version: viewVersion,
    };

    const saveResult = await saveViewDefinition(view);
    if (!saveResult.hasError) {
      const newView = saveResult.viewDefinition;
      // Updates view validation state and results
      updateViewStatusAndResults(newView!);
      setViewVersion(newView!.version);

      setIsSaving(false);
      return true;
    } else {
      setIsSaving(false);
      const validationResult = {
        message: saveResult.versionConflict
          ? t('viewSaveVersionConflictMessage')
          : t('viewSaveErrorMessage'),
        type: 'danger',
      } as IViewEditValidationResult;
      setNoResultsTitle(t('preview.resultsTableInvalidEmptyTitle'));
      setNoResultsMessage(t('preview.resultsTableInvalidEmptyInfo'));
      setValidationResultsTitle(t('viewSaveErrorTitle'));
      setValidationResults([validationResult]);
      setValidationMessageVisible(true);
      setViewValid(false);
      updateQueryResults(false);
      return false;
    }
  };

  const handlePreviewExpandedChanged = (expanded: boolean) => {
    setPreviewExpanded(expanded);
  };

  const handleHideValidationMessage = () => {
    setValidationMessageVisible(false);
  };

  const handleEditFinished = () => {
    history.push(
      resolvers.data.virtualizations.views.root({
        virtualization: state.virtualization,
      })
    );
  };

  /**
   * Callback for when the dirty state of the DDL editor changes.
   * @param dirty `true` if DDL editor has changes
   */
  const handleDirtyStateChanged = (dirty: boolean) => {
    ddlHasChanges.current = dirty;
  };

  const handleRefreshResults = async () => {
    updateQueryResults(viewValid, true);
  };

  /**
   * Update the preview query results
   * @param isValid 'true' if the view definition is valid
   * @param refreshClick 'true' if update is initiated by the preview refresh button
   */
  const updateQueryResults = async (
    isValid: boolean,
    refreshClick: boolean = false
  ) => {
    setIsLoadingPreview(true);
    let queryResult = queryResultsEmpty;
    try {
      // Valid view - run the preview query
      if (isValid) {
        queryResult = await queryVirtualization(
          params.virtualizationId,
          getPreviewSql(viewDefn.name),
          15,
          0
        );
      }
      setIsLoadingPreview(false);
      setQueryResults(queryResult);
    } catch (error) {
      setQueryResults(queryResult);
      setIsLoadingPreview(false);
      if (refreshClick) {
        const details = error.message ? error.message : '';
        pushNotification(
          t('queryViewFailed', {
            details,
            name: viewDefn.name,
          }),
          'error'
        );
      }
    }
  };

  // load source table/column information
  React.useEffect(() => {
    if (
      !isMetadataLoaded &&
      virtualization !== null &&
      (virtualization as Virtualization).name !== null &&
      (virtualization as Virtualization).name.length > 0
    ) {
      // load source table/column info by retrieving the view source info from
      // the server and converting to TableColumn objects
      const loadSourceTableInfo = async () => {
        try {
          const results: ViewSourceInfo = await getSourceInfoForView(
            virtualization.name
          );
          setSourceTableColumns(
            generateTableColumns(results as ViewSourceInfo)
          );
          setSourceInfo(results.schemas);
        } catch (error) {
          pushNotification(error.message, 'error');
        }
      };
      loadSourceTableInfo();
      handleMetadataLoaded();
    }
    // eslint-disable-next-line
  }, [virtualization as Virtualization]);

  // initial load of query results
  React.useEffect(() => {
    if (
      !isQueryResultsLoaded &&
      viewDefn !== null &&
      (viewDefn as ViewDefinition).name !== null &&
      (viewDefn as ViewDefinition).name.length > 0
    ) {
      updateViewStatusAndResults(viewDefn);
      handleQueryResultsLoaded();
    }
    // eslint-disable-next-line
  }, [viewDefn as ViewDefinition]);

  const shouldDisplayDialog = React.useCallback(() => {
    return ddlHasChanges.current;
  }, [ddlHasChanges]);

  return (
    <WithLoader
      loading={loading}
      loaderChildren={<PageLoader />}
      error={error !== false}
      errorChildren={<ApiError error={error as Error} />}
    >
      {() => (
        <WithLeaveConfirmation
          i18nTitle={t('viewEditor.unsavedChangesTitle')}
          i18nConfirmationMessage={t('viewEditor.unsavedChangesMessage')}
          shouldDisplayDialog={shouldDisplayDialog}
        >
          {() => (
            <>
              <Breadcrumb>
                <Link to={resolvers.dashboard.root()}>{t('shared:Home')}</Link>
                <Link to={resolvers.data.root()}>{t('shared:Data')}</Link>
                <Link
                  to={resolvers.data.virtualizations.views.root({
                    virtualization,
                  })}
                >
                  {t('virtualizationNameBreadcrumb', {
                    name: virtualization.name,
                  })}
                </Link>
                <span>{t('viewNameBreadcrumb', { name: viewDefn.name })}</span>
              </Breadcrumb>
              <ApiContext.Consumer>
                {({ dvApiUri, headers }) => (
                  <DdlEditor
                    viewDdl={viewDefn.ddl ? viewDefn.ddl : ''}
                    i18nCursorColumn={t('cursorColumn')}
                    i18nCursorLine={t('cursorLine')}
                    i18nDdlTextPlaceholder={t('ddlTextPlaceholder')}
                    i18nDoneLabel={t('shared:Done')}
                    i18nSaveLabel={t('shared:Save')}
                    i18nTitle={t('viewEditor.title')}
                    i18nMetadataTitle={t('metadataTree')}
                    i18nLoading={t('shared:Loading')}
                    previewExpanded={previewExpanded}
                    languageServerUrl={`${dvApiUri}teiid-ddl-language-server`}
                    i18nValidationResultsTitle={validationResultsTitle}
                    showValidationMessage={validationMessageVisible}
                    isSaving={isSaving}
                    sourceTableInfos={sourceTableColumns}
                    sourceInfo={sourceInfo}
                    onCloseValidationMessage={handleHideValidationMessage}
                    onFinish={handleEditFinished}
                    onSave={handleSaveView}
                    setDirty={handleDirtyStateChanged}
                    validationResults={validationResults}
                  />
                )}
              </ApiContext.Consumer>
              <ExpandablePreview
                i18nEmptyResultsTitle={noResultsTitle}
                i18nEmptyResultsMsg={noResultsMessage}
                i18nHidePreview={t('preview.hidePreview')}
                i18nLoadingQueryResults={t('preview.loadingQueryResults')}
                i18nRowTotalLabel={t('queryResultsRowCountMsg')}
                i18nShowPreview={t('preview.showPreview')}
                i18nTitle={t('Refresh')}
                initialExpanded={previewExpanded}
                isLoadingPreview={isLoadingPreview}
                onPreviewExpandedChanged={handlePreviewExpandedChanged}
                onRefreshResults={handleRefreshResults}
                queryResultRows={getQueryRows(queryResults)}
                queryResultCols={getQueryColumns(queryResults)}
              />
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </WithLoader>
  );
};
