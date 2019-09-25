import {
  useViewDefinition,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import {
  QueryResults,
  RestDataService,
  ViewDefinition,
  ViewSourceInfo,
} from '@syndesis/models';
import { TableColumns } from '@syndesis/models';
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
 * @param previewExpanded - the state of preview component expansion
 * @param queryResults - the current query results in the preview component
 */
export interface IViewEditorSqlRouteState {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
  previewExpanded: boolean;
  queryResults: QueryResults;
}

export const ViewEditorSqlPage: React.FunctionComponent = () => {
  const [isSaving, setIsSaving] = React.useState(false);
  const [isLoadingPreview, setIsLoadingPreview] = React.useState(false);
  const [isMetadataLoaded, setMetadataLoaded] = React.useState(false);
  const [sourceTableColumns, setSourceTableColumns] = React.useState<
    TableColumns[]
  >([]);
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
  const { params, state, history } = useRouteData<
    IViewEditorSqlRouteParams,
    IViewEditorSqlRouteState
  >();
  const {
    getSourceInfoForView,
    queryVirtualization,
    saveViewDefinition,
    validateViewDefinition,
  } = useVirtualizationHelpers();
  const [previewExpanded, setPreviewExpanded] = React.useState(
    state.previewExpanded
  );
  const [queryResults, setQueryResults] = React.useState(state.queryResults);
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );
  const { resource: viewDefn, loading, error } = useViewDefinition(
    params.viewDefinitionId,
    state.viewDefinition
  );
  const [noResultsTitle, setNoResultsTitle] = React.useState(
    t('data:virtualization.preview.resultsTableValidEmptyTitle')
  );
  const [noResultsMessage, setNoResultsMessage] = React.useState(
    t('data:virtualization.preview.resultsTableValidEmptyInfo')
  );
  const ddlHasChanges = React.useRef(false);

  const queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };

  const handleMetadataLoaded = async (): Promise<void> => {
    if (sourceTableColumns != null && sourceTableColumns.length > 0) {
      setMetadataLoaded(true);
    }
  };

  /**
   * Validate the view and update preview results
   * @param view the view definiton
   */
  const handleValidateView = async (view: ViewDefinition) => {
    // Validate the View
    const validationResponse = await validateViewDefinition(view);
    let validationResult = null;
    if (validationResponse.status === 'SUCCESS') {
      validationResult = {
        message: 'Validation successful',
        type: 'success',
      } as IViewEditValidationResult;
    } else {
      validationResult = {
        message: validationResponse.message,
        type: 'danger',
      } as IViewEditValidationResult;
    }
    const isValid = validationResult.type === 'success';
    setQueryResultEmptyContent(isValid);
    setDdlValidationInfo(validationResult, isValid);
    updateQueryResults(isValid);
  };

  // Preview result empty content changes if view is invalid
  const setQueryResultEmptyContent = (isValid: boolean) => {
    if (isValid) {
      setNoResultsTitle(
        t('data:virtualization.preview.resultsTableValidEmptyTitle')
      );
      setNoResultsMessage(
        t('data:virtualization.preview.resultsTableValidEmptyInfo')
      );
    } else {
      setNoResultsTitle(
        t('data:virtualization.preview.resultsTableInvalidEmptyTitle')
      );
      setNoResultsMessage(
        t('data:virtualization.preview.resultsTableInvalidEmptyInfo')
      );
    }
  };

  // Update info for the DDL editor
  const setDdlValidationInfo = (
    validationResult: IViewEditValidationResult,
    isValid: boolean
  ) => {
    setValidationResults([validationResult]);
    setValidationMessageVisible(!isValid);
    setViewValid(isValid);
  };

  /**
   * Saves View with the new DDL value.  The View is also validated, and preview results updated
   */
  const handleSaveView = async (ddlValue: string): Promise<boolean> => {
    setIsSaving(true);
    let saveSuccess = false;

    // View Definition
    const view: ViewDefinition = {
      dataVirtualizationName: viewDefn.dataVirtualizationName,
      ddl: ddlValue,
      id: viewDefn.id,
      isComplete: viewDefn.isComplete,
      isUserDefined: true,
      keng__description: viewDefn.keng__description,
      name: viewDefn.name,
      sourcePaths: viewDefn.sourcePaths,
    };

    try {
      // Save the View
      await saveViewDefinition(view);
      saveSuccess = true;

      // Validate the View
      await handleValidateView(view);

      setIsSaving(false);
      return true;
    } catch (error) {
      const details = error.message ? error.message : '';
      setIsSaving(false);
      if (saveSuccess) {
        pushNotification(
          t('virtualization.viewValidationFailed', {
            details,
            name: viewDefn.name,
          }),
          'error'
        );
      } else {
        pushNotification(
          t('virtualization.saveViewFailed', {
            details,
            name: viewDefn.name,
          }),
          'error'
        );
      }
      return saveSuccess;
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
          getPreviewSql(viewDefn),
          15,
          0
        );
      }
      setIsLoadingPreview(false);
      setQueryResults(queryResult);
      // Show toast for refresh click
      if (refreshClick) {
        pushNotification(
          t('virtualization.queryResultsRefreshed', {
            name: viewDefn.name,
          }),
          'success'
        );
      }
    } catch (error) {
      setQueryResults(queryResult);
      setIsLoadingPreview(false);
      if (refreshClick) {
        const details = error.message ? error.message : '';
        pushNotification(
          t('virtualization.queryViewFailed', {
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
      (virtualization as RestDataService).keng__id !== null &&
      (virtualization as RestDataService).keng__id.length > 0
    ) {
      // load source table/column info by retrieving the view source info from
      // the server and converting to TableColumn objects
      const loadSourceTableInfo = async () => {
        try {
          const results: ViewSourceInfo = await getSourceInfoForView(
            virtualization
          );
          setSourceTableColumns(
            generateTableColumns(results as ViewSourceInfo)
          );
        } catch (error) {
          pushNotification(error.message, 'error');
        }
      };
      loadSourceTableInfo();
      handleMetadataLoaded();
    }
    // eslint-disable-next-line
  }, [virtualization as RestDataService]);

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
          i18nTitle={t('virtualization.viewEditor.unsavedChangesTitle')}
          i18nConfirmationMessage={t(
            'virtualization.viewEditor.unsavedChangesMessage'
          )}
          shouldDisplayDialog={shouldDisplayDialog}
        >
          {() => (
            <>
              <Breadcrumb>
                <Link to={resolvers.dashboard.root()}>{t('shared:Home')}</Link>
                <Link to={resolvers.data.root()}>
                  {t('shared:DataVirtualizations')}
                </Link>
                <Link
                  to={resolvers.data.virtualizations.views.root({
                    virtualization,
                  })}
                >
                  {virtualization.keng__id}
                </Link>
                <span>{viewDefn.name}</span>
              </Breadcrumb>
              <DdlEditor
                viewDdl={viewDefn.ddl ? viewDefn.ddl : ''}
                i18nDoneLabel={t('shared:Done')}
                i18nSaveLabel={t('shared:Save')}
                i18nTitle={t('data:virtualization.viewEditor.title')}
                i18nValidationResultsTitle={t(
                  'data:virtualization.validationResultsTitle'
                )}
                showValidationMessage={validationMessageVisible}
                isSaving={isSaving}
                sourceTableInfos={sourceTableColumns}
                onCloseValidationMessage={handleHideValidationMessage}
                onFinish={handleEditFinished}
                onSave={handleSaveView}
                setDirty={handleDirtyStateChanged}
                validationResults={validationResults}
              />
              <ExpandablePreview
                i18nEmptyResultsTitle={noResultsTitle}
                i18nEmptyResultsMsg={noResultsMessage}
                i18nHidePreview={t('data:virtualization.preview.hidePreview')}
                i18nLoadingQueryResults={t(
                  'data:virtualization.preview.loadingQueryResults'
                )}
                i18nRowTotalLabel={t('virtualization.queryResultsRowCountMsg')}
                i18nShowPreview={t('data:virtualization.preview.showPreview')}
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
