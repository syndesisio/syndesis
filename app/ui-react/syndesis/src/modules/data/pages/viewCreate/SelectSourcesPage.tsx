import { CubeIcon } from '@patternfly/react-icons';
import {
  useConnections,
  useVirtualizationConnectionStatuses,
  useVirtualizationHelpers,
  useVirtualizationRuntimeMetadata,
} from '@syndesis/api';
import { Connection, SchemaNodeInfo, TableInfo, ViewSourceInfo, Virtualization } from '@syndesis/models';
import { QueryResults, VirtualizationSourceStatus } from '@syndesis/models/src';
import { PreviewData, ViewCreateLayout, ViewWizardHeader } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { EntityIcon, PageTitle } from '../../../../shared';
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
    isVirtualizationSchema: boolean,
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

  /**
   * Get schema for the virtualization
   */
  const getVirtualizationSchema = (sourceInfo: ViewSourceInfo) => {
    return sourceInfo.schemas.find(schema => schema.name === virtualization.name);
  };

  /**
   * Get the Connection Statuses - and add a connection status for the Virtualization
   */
  const getConnectionStatusesAddVirtualization = (statuses: VirtualizationSourceStatus[]) => {
    const virtSourceStatus: VirtualizationSourceStatus = {
      errors: [],
      id: virtualization.name,
      isVirtualizationSource: true,
      lastLoad: 0,
      loading: false,
      schemaState: 'ACTIVE',
      sourceName: virtualization.name,
      teiidName: virtualization.name,
    }
    return [...statuses, virtSourceStatus];
  };

  /**
   * Get the Connections - and include a connection for the Virtualization
   */
  const getConnectionsForDisplay = (conns: Connection[]) => {
    // If a virtualization has been published, it will have a connection.  If so, remove it - we will use virtualization metadata.
    const tempConns = conns.slice();
    const index = tempConns.findIndex(conn => conn.name === virtualization.name);
    if (index > -1) {
      tempConns.splice(index, 1);
    }

    // Add 'connection' for the virtualization
    const virtConnection: Connection = {
      description: virtualization.description,
      name: virtualization.name,
    };
    return [...tempConns, virtConnection];
  };
  
  const getConnectionIcons = (conns: Connection[], size: number) => {
    const iconMap: Map<string, JSX.Element> = new Map();
    // Set icons for the connections
    for(const theConn of conns) {
      const icon = <EntityIcon entity={theConn} alt={theConn.name} width={size} />;
      iconMap.set(theConn.name, icon);
    }
    // Add the virtualization icon
    const iconsize = size>15 ? size>20 ? 'lg': 'md' : 'sm';
    const virtIcon = <CubeIcon size={iconsize} />;
    iconMap.set(virtualization.name, virtIcon);
    return iconMap;
  };

  return (
    <>
    <PageTitle title={t('createViewPageTitle')} />
    <ViewCreateLayout
      header={
        <ViewWizardHeader
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
          i18nStep1Text={t('shared:ChooseTable')}
          i18nStep2Text={t('shared:NameYourView')}
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
          dvSourceStatuses={getConnectionStatusesAddVirtualization(connectionStatuses)}
          connections={getConnectionsForDisplay(connectionsData.connectionsForDisplay)}
          connectionIcons={getConnectionIcons(connectionsData.connectionsForDisplay, 23)}
          virtualizationSchema={getVirtualizationSchema(viewSourceInfo)}
          onNodeSelected={props.handleNodeSelected}
          onNodeDeselected={onTableDeselect}
          selectedSchemaNodes={props.selectedSchemaNodes}
        />
      }
      selectedTables={
        <ConnectionTables
          selectedSchemaNodes={props.selectedSchemaNodes}
          connectionIcons={getConnectionIcons(connectionsData.connectionsForDisplay, 12)}
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
            name: previewTable.tableName
          })}
          connectionName={previewTable.connectionName}
          connectionIcon={getConnectionIcons(connectionsData.connectionsForDisplay, 17).get(previewTable.connectionName)}
          isLoadingPreview={isLoadingPreview}
          isExpanded={isExpanded}
          onToggle={onToggle}
        />
      }
    />
  </>
  );
};
