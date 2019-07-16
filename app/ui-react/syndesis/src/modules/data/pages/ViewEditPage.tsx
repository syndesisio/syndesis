import { useVirtualizationHelpers } from '@syndesis/api';
import { TableColumns } from '@syndesis/models';
import {
  RestDataService,
  ViewDefinition,
  ViewEditorState,
} from '@syndesis/models';
import {
  Breadcrumb,
  IViewEditValidationResult,
  ViewEditContent,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import resolvers from '../../resolvers';

/**
 * @param virtualization - the Virtualization
 * @param editorState - the ViewDefinition
 */
export interface IViewEditRouteState {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
}

export const ViewEditPage: React.FunctionComponent = () => {

  const [isWorking, setIsWorking] = React.useState(false);
  const [viewValid, setViewValid] = React.useState(true);
  const [validationResults, setValidationResults] = React.useState<IViewEditValidationResult[]>([]);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { state, history } = useRouteData<null, IViewEditRouteState>();
  const { refreshVirtualizationViews, validateViewDefinition } = useVirtualizationHelpers();

  const handleValidationStarted = async (): Promise<void> => {
    setIsWorking(true);
  };

  const handleValidationComplete = async (
    validation: IViewEditValidationResult
  ): Promise<void> => {
    setIsWorking(false);
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
    // View Definition
    const viewDefn: ViewDefinition = {
      compositions: state.viewDefinition.compositions,
      ddl: ddlValue,
      isComplete: state.viewDefinition.isComplete,
      isUserDefined: true,
      keng__description: state.viewDefinition.keng__description,
      projectedColumns: state.viewDefinition.projectedColumns,
      sourcePaths: state.viewDefinition.sourcePaths,
      viewName: state.viewDefinition.viewName,
    };

    const viewEditorState: ViewEditorState = {
      id:
        state.virtualization.serviceVdbName +
        '.' +
        state.viewDefinition.viewName,
      viewDefinition: viewDefn,
    };
    try {
      await refreshVirtualizationViews(
        state.virtualization.keng__id,
        [viewEditorState]
      );
      pushNotification(
        t(
          'virtualization.saveViewSuccess',
          { name: state.viewDefinition.viewName }
        ),
        'success'
      );
      // redirect to views page on success
      handleSelectVirtualization(state.virtualization);
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
      pushNotification(
        t('virtualization.saveViewFailed', {
          details,
          name: state.viewDefinition.viewName,
        }),
        'error'
      );
    }
  };

  // Validate the View using the new DDL
  const handleValidateView = async (ddlValue: string) => {
    handleValidationStarted();

    // View Definition
    const viewDefn: ViewDefinition = {
      compositions: state.viewDefinition.compositions,
      ddl: ddlValue,
      isComplete: state.viewDefinition.isComplete,
      isUserDefined: state.viewDefinition.isUserDefined,
      keng__description: state.viewDefinition.keng__description,
      projectedColumns: state.viewDefinition.projectedColumns,
      sourcePaths: state.viewDefinition.sourcePaths,
      viewName: state.viewDefinition.viewName,
    };

    const validationResponse = await validateViewDefinition(
      viewDefn
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
    handleSelectVirtualization(state.virtualization);
  };

  const initialView = state.viewDefinition.ddl ? state.viewDefinition.ddl : '';
  const virtualization = state.virtualization;

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

  return (
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
          {state.virtualization.keng__id}
        </Link>
        <span>{state.viewDefinition.viewName}</span>
      </Breadcrumb>
      <ViewEditContent
        viewDdl={initialView}
        i18nCancelLabel={t('shared:Cancel')}
        i18nSaveLabel={t('shared:Save')}
        i18nTitle={t('virtualization.viewEditorTitle')}
        i18nDescription={t('virtualization.viewEditorDescription')}
        i18nValidateLabel={t('shared:Validate')}
        isValid={viewValid}
        isWorking={isWorking}
        sourceTableInfos={getSourceTableInfos()}
        onCancel={handleCancel}
        onValidate={handleValidateView}
        onSave={handleSaveView}
        validationResults={
          validationResults
        }
      />
    </>
  );
}
