import {
  useConnections,
  useVirtualizationConnectionStatuses,
  useVirtualizationHelpers,
  useVirtualizationRuntimeMetadata,
} from '@syndesis/api';
import { SchemaNodeInfo, TableInfo, Virtualization } from '@syndesis/models';
import { QueryResults } from '@syndesis/models/src';
import { CreateViewHeader, PreviewData, ViewCreateLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import resolvers from '../../../resolvers';
import { getQueryColumns, getQueryRows } from '../../shared';
import {
  ConnectionSchemaContent,
  ConnectionTables,
  getPreviewSql,
} from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard
 */
export interface ISelectSourcesRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 */
export interface ISelectSourcesRouteState {
  virtualization: Virtualization;
}

export interface ISelectSourcesPageProps {
  handleNodeSelected: (
    connectionName: string,
    name: string,
    teiidName: string,
    nodePath: string[]
  ) => void;
  handleNodeDeselected: (connectionName: string, teiidName: string) => void;
  selectedSchemaNodes: SchemaNodeInfo[];
}

export const SelectSourcesPage: React.FunctionComponent<ISelectSourcesPageProps> = props => {
  const { t } = useTranslation(['data', 'shared']);
  const schemaNodeInfo: SchemaNodeInfo[] = props.selectedSchemaNodes;
  const { pushNotification } = React.useContext(UIContext);
  const queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };
  const emptyTableInfo : TableInfo = {connectionName:'', tableName:''};

  /* State used in component */
  const [showPreviewData, setShowPreviewData] = React.useState<boolean>(false);
  const [isLoadingPreview, setIsLoadingPreview] = React.useState<boolean>(false);
  const [previewTable,setPreviewTable] = React.useState<TableInfo>(emptyTableInfo);
  const [isExpanded, setIsExpanded] = React.useState<boolean>(true);
  const [queryResults, setQueryResults] = React.useState(queryResultsEmpty);
  const { state } = useRouteData<null, ISelectSourcesRouteState>();

  const virtualization = state.virtualization;

  const onToggle = () => {
    setIsExpanded(!isExpanded);
  };

  const onTableDeselect = (connectionName: string,
    teiidName: string) =>{
    props.handleNodeDeselected(connectionName,teiidName);
    if(previewTable.connectionName ===connectionName && previewTable.tableName ===teiidName){
      setShowPreviewData(false);
      setPreviewTable(emptyTableInfo);
    }
  }

  /* API Request */
  const { queryVirtualization } = useVirtualizationHelpers();
  const {
    resource: connectionStatuses,
    hasData: hasConnectionStatuses,
    error: connectionStatusesError,
  } = useVirtualizationConnectionStatuses();
  const {
    resource: connectionsData,
    hasData: hasConnectionsData,
    error: connectionsError,
  } = useConnections();
  const {
    resource: viewSourceInfo,
    hasData: hasViewSourceInfo,
    error: viewSourceInfoError,
  } = useVirtualizationRuntimeMetadata(virtualization.name);

  const toggleShowPreviewData = async (
    cName: string,
    selectedTableName: string
  ) => {
    const selectedTableInfo : TableInfo ={
      connectionName: cName,
      tableName: selectedTableName
    };
    setIsLoadingPreview(true);
    setShowPreviewData(true);
    setPreviewTable(selectedTableInfo)
    try {
      const queryResult = await queryVirtualization(
        virtualization.name,
        getPreviewSql(`${cName}.${selectedTableName}`),
        15,
        0
      );
      setIsLoadingPreview(false);
      setQueryResults(queryResult);
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('queryTableFailed', {
          details,
          name: virtualization.name,
        }),
        'error'
      );
      setIsLoadingPreview(false);
    }
  };

  /**
   * Get error message based on the required fetches
   */
  const getErrorMessage = () => {
    if (connectionsError !== false) {
      return (connectionsError as Error).message;
    } else if (connectionStatusesError !== false) {
      return (connectionStatusesError as Error).message;
    } else if (viewSourceInfoError !== false) {
      return (viewSourceInfoError as Error).message;
    }
    return undefined;
  };

  return (
    <ViewCreateLayout
      header={
        <CreateViewHeader
          step={1}
          cancelHref={resolvers.data.virtualizations.views.root({
            virtualization,
          })}
          nextHref={resolvers.data.virtualizations.views.createView.selectName({
            schemaNodeInfo,
            virtualization,
          })}
          isNextDisabled={false}
          isNextLoading={false}
          isLastStep={false}
          i18nChooseTable={t('shared:ChooseTable')}
          i18nNameYourView={t('shared:NameYourView')}
          i18nBack={t('shared:Back')}
          i18nDone={t('shared:Done')}
          i18nNext={t('shared:Next')}
          i18nCancel={t('shared:Cancel')}
        />
      }
      content={
        <ConnectionSchemaContent
          error={
            connectionsError !== false ||
            connectionStatusesError !== false ||
            viewSourceInfoError !== false
          }
          errorMessage={getErrorMessage()}
          loading={
            !hasConnectionsData || !hasConnectionStatuses || !hasViewSourceInfo
          }
          dvSourceStatuses={connectionStatuses}
          connections={connectionsData.connectionsForDisplay}
          onNodeSelected={props.handleNodeSelected}
          onNodeDeselected={onTableDeselect}
          selectedSchemaNodes={props.selectedSchemaNodes}
        />
      }
      selectedTables={
        <ConnectionTables
          selectedSchemaNodes={props.selectedSchemaNodes}
          onNodeDeselected={onTableDeselect}
          columnDetails={viewSourceInfo.schemas}
          setShowPreviewData={toggleShowPreviewData}
        />
      }
      showPreviewData={showPreviewData}
      previewData={
        <PreviewData
          queryResultCols={getQueryColumns(queryResults)}
          queryResultRows={getQueryRows(queryResults)}
          i18nEmptyResultsTitle={t('preview.resultsTableValidEmptyTitle')}
          i18nEmptyResultsMsg={t('preview.resultsTableValidEmptyInfo')}
          i18nLoadingQueryResults={t('preview.loadingQueryResults')}
          i18nHidePreview={t('preview.hidePreview')}
          i18nShowPreview={t('preview.showPreview')}
          i18nPreviewHeading={t('preview.previewHeading', {
            connection: previewTable.connectionName,
            name: previewTable.tableName
          })}
          isLoadingPreview={isLoadingPreview}
          isExpanded={isExpanded}
          onToggle={onToggle}
        />
      }
    />
  );
};
