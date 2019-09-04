import { useViewDefinition, useVirtualization } from '@syndesis/api';
import { useVirtualizationHelpers } from '@syndesis/api';
import {
  QueryResults,
  RestDataService,
  ViewDefinition,
} from '@syndesis/models';
import { TableColumns } from '@syndesis/models';
import {
  Breadcrumb,
  DdlEditor,
  IViewEditValidationResult,
} from '@syndesis/ui';
import { 
  ExpandablePreview, 
  PageLoader, 
  PageSection, 
  PreviewButtonSelection, 
  ViewEditorNavBar 
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../../app';
import { ApiError } from '../../../../shared';
import resolvers from '../../../resolvers';
import { getPreviewSql, getQueryColumns, getQueryRows } from '../../shared/VirtualizationUtils';

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
 * @param previewButtonSelection - the button selection state in the preview component
 * @param queryResults - the current query results in the preview component
 */
export interface IViewEditorSqlRouteState {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
  previewExpanded: boolean;
  previewButtonSelection: PreviewButtonSelection;
  queryResults: QueryResults;
}

export const ViewEditorSqlPage: React.FunctionComponent = () => {

  const [isSaving, setIsSaving] = React.useState(false);
  const [isValidating, setIsValidating] = React.useState(false);
  const [viewValid, setViewValid] = React.useState(true);
  const [validationResults, setValidationResults] = React.useState<IViewEditValidationResult[]>([]);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<IViewEditorSqlRouteParams, IViewEditorSqlRouteState>();
  const { queryVirtualization, saveViewDefinition, validateViewDefinition } = useVirtualizationHelpers();
  const [previewExpanded, setPreviewExpanded] = React.useState(state.previewExpanded);
  const [queryResults, setQueryResults] = React.useState(state.queryResults);
  const [previewButtonSelection, setPreviewButtonSelection] = React.useState(state.previewButtonSelection);
  const { resource: virtualization } = useVirtualization(params.virtualizationId);
  const { resource: viewDefn, loading, error } = useViewDefinition(params.viewDefinitionId, state.viewDefinition);

  const handleValidationStarted = async (): Promise<void> => {
    setIsValidating(true);
  };

  const handleValidationComplete = async (
    validation: IViewEditValidationResult
  ): Promise<void> => {
    setIsValidating(false);
    setValidationResults([validation]);
    setViewValid(validation.type === 'success');
  };

  // Save the View with new DDL and description
  const handleSaveView = async (ddlValue: string) => {
    setIsSaving(true);
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
      await saveViewDefinition(view);
      setIsSaving(false);
      pushNotification(
        t(
          'virtualization.saveViewSuccess',
          { name: viewDefn.name,
        }),
        'success'
      );
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
        setIsSaving(false);
        pushNotification(
        t('virtualization.saveViewFailed', {
          details,
          name: viewDefn.name,
        }),
        'error'
      );
    }
  };

  // Validate the View using the new DDL
  const handleValidateView = async (ddlValue: string) => {
    handleValidationStarted();

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

    const validationResponse = await validateViewDefinition(
      view
    );
    if (validationResponse.status === 'SUCCESS') {
      const validationResult = {
        message: 'Validation successful',
        type: 'success',
      } as IViewEditValidationResult;
      handleValidationComplete(validationResult);
    } else {
      const validationResult = {
        message: validationResponse.message,
        type: 'danger',
      } as IViewEditValidationResult;

      handleValidationComplete(validationResult);
    }
  };

  const getSourceTableInfos = (): TableColumns[] => {
    // TODO: replace this hardcoded data with server call (or table-column info on the virtualization)
    const sourceTables = [
      { 'columnNames': ['name', 'population', 'size'], // column names
        'name': 'countries' },                         // table name
      { 'columnNames': ['name','score', 'birthDate'],  
        'name': 'users' 
      }
    ];
    return sourceTables;
  };

  const handlePreviewExpandedChanged = (
    expanded: boolean
  ) => {
    setPreviewExpanded(expanded);
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
  };

  const handleRefreshResults = async () => {
    try {
      const sqlStatement = getPreviewSql(viewDefn);

      const results: QueryResults = await queryVirtualization(
        params.virtualizationId,
        sqlStatement,
        15,
        0
      );
      pushNotification(
        t('virtualization.queryViewSuccess', {
          name: viewDefn.name,
        }),
        'success'
      );
      setQueryResults(results);
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.queryViewFailed', {
          details,
          name: viewDefn.name,
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
              {virtualization.keng__id}
            </Link>
            <span>{viewDefn.name}</span>
          </Breadcrumb>
          <PageSection variant={'light'} noPadding={true}>
            <ViewEditorNavBar
              i18nFinishButton={t('data:virtualization.viewEditor.Done')}
              i18nViewOutputTab={t('data:virtualization.viewEditor.viewOutputTab')}
              i18nViewSqlTab={t('data:virtualization.viewEditor.sqlTab')}
              viewOutputHref={resolvers.data.virtualizations.views.edit.viewOutput({
                virtualization,
                // tslint:disable-next-line: object-literal-sort-keys
                viewDefinitionId: params.viewDefinitionId,
                previewExpanded,
                viewDefinition: viewDefn,
                previewButtonSelection,
                queryResults,
              })}
              viewSqlHref={resolvers.data.virtualizations.views.edit.sql({
                virtualization,
                // tslint:disable-next-line: object-literal-sort-keys
                viewDefinitionId: params.viewDefinitionId,
                previewExpanded,
                viewDefinition: viewDefn,
                previewButtonSelection,
                queryResults,
              })}
              onEditFinished={handleEditFinished}            
            />
          </PageSection>
          <DdlEditor
            viewDdl={viewDefn.ddl ? viewDefn.ddl : ''}
            i18nSaveLabel={t('shared:Save')}
            i18nValidateLabel={t('shared:Validate')}
            i18nValidationResultsTitle={t('data:virtualization.validationResultsTitle')}
            isValid={viewValid}
            isSaving={isSaving}
            isValidating={isValidating}
            sourceTableInfos={getSourceTableInfos()}
            onValidate={handleValidateView}
            onSave={handleSaveView}
            validationResults={
              validationResults
            }
          />
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
              viewDdl={viewDefn.ddl ? viewDefn.ddl : ''}
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
}
