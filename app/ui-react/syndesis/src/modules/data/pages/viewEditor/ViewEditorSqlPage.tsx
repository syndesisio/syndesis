import { useViewDefinition, useVirtualization } from '@syndesis/api';
import { useVirtualizationHelpers } from '@syndesis/api';
import {
  RestDataService,
  ViewDefinition,
} from '@syndesis/models';
import { TableColumns } from '@syndesis/models';
import {
  Breadcrumb,
  DdlEditor,
  IViewEditValidationResult,
} from '@syndesis/ui';
import { ExpandablePreview, PageLoader, PageSection } from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../../app';
import { ApiError } from '../../../../shared';
import resolvers from '../../../resolvers';
import {
  ViewEditorNavBar
} from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization that the view belongs to
 * @param viewDefinitionId - the name of the view being edited
 */
export interface IViewEditorSqlRouteParams {
  virtualizationId: string;
  viewDefinitionId: string;
}

/**
 * @param previewExpanded - expanded state of the preview area
 * @param viewDefinition - the ViewDefinition
 */
export interface IViewEditorSqlRouteState {
  previewExpanded: boolean;
  viewDefinition?: ViewDefinition;
}

export const ViewEditorSqlPage: React.FunctionComponent = () => {

  const [isSaving, setIsSaving] = React.useState(false);
  const [isValidating, setIsValidating] = React.useState(false);
  const [viewValid, setViewValid] = React.useState(true);
  const [validationResults, setValidationResults] = React.useState<IViewEditValidationResult[]>([]);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<IViewEditorSqlRouteParams, IViewEditorSqlRouteState>();
  const { saveViewDefinition, validateViewDefinition } = useVirtualizationHelpers();
  const [previewExpanded, setPreviewExpanded] = React.useState(state.previewExpanded);
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

  // tslint:disable-next-line: no-shadowed-variable
  const handleSelectVirtualization = (virtualization: RestDataService) => {
    // redirect to virtualization views page
    history.push(
      resolvers.data.virtualizations.views.root({
        virtualization,
      })
    );
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
      // redirect to views page on success
      handleSelectVirtualization(virtualization);
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
        type: 'error',
      } as IViewEditValidationResult;
      handleValidationComplete(validationResult);
    }
  };

  const handleCancel = () => {
    // redirect to views page
    handleSelectVirtualization(virtualization);
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
              virtualization={virtualization}
              viewDefinitionId={params.viewDefinitionId}
              viewDefinition={viewDefn}
              previewExpanded={previewExpanded}
            />
          </PageSection>
          <DdlEditor
            viewDdl={viewDefn.ddl ? viewDefn.ddl : ''}
            i18nCancelLabel={t('shared:Cancel')}
            i18nSaveLabel={t('shared:Save')}
            i18nValidateLabel={t('shared:Validate')}
            isValid={viewValid}
            isSaving={isSaving}
            isValidating={isValidating}
            sourceTableInfos={getSourceTableInfos()}
            onCancel={handleCancel}
            onValidate={handleValidateView}
            onSave={handleSaveView}
            validationResults={
              validationResults
            }
          />
          <PageSection variant={'light'} noPadding={true}>
            <ExpandablePreview
              i18nHidePreview={t('data:virtualization.preview.hidePreview')}
              i18nShowPreview={t('data:virtualization.preview.showPreview')}
              initialExpanded={previewExpanded}
              onPreviewExpandedChanged={handlePreviewExpandedChanged}
            />
          </PageSection>
        </>
      )}
    </WithLoader>
  );
}
