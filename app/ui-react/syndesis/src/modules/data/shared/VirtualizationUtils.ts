import {
  Connection,
  QueryResults,
  RestDataService,
  SchemaNode,
  SchemaNodeInfo,
  ViewDefinition,
  ViewInfo,
  ViewSourceInfo,
  VirtualizationPublishingDetails,
  VirtualizationSourceStatus,
} from '@syndesis/models';
import { ITableInfo } from '@syndesis/ui';

interface IColumn {
  id: string;
  label: string;
}

export enum DvConnectionStatus {
  ACTIVE = 'ACTIVE',
  FAILED = 'FAILED',
  INACTIVE = 'INACTIVE',
}

/**
 * Recursively flattens the tree structure of SchemaNodes,
 * into an array of ViewInfos
 * @param viewInfos the array of ViewInfos
 * @param schemaNode the SchemaNode from which the ViewInfo is generated
 * @param nodePath path for current SchemaNode eg ['name0', 'name1', 'name2']
 * @param selectedViewNames names of views which are selected
 * @param existingViewNames names of views which exist (marked as update)
 */
export function generateViewInfos(
  viewInfos: ViewInfo[],
  schemaNode: SchemaNode,
  nodePath: string[],
  selectedViewNames: string[],
  existingViewNames: string[]
): void {
  if (schemaNode) {
    // Generate source path from nodePath array
    const sourcePath: string[] = [];
    for (const seg of nodePath) {
      sourcePath.push(seg);
    }

    // Creates ViewInfo if the SchemaNode is queryable
    if (schemaNode.queryable === true) {
      const vwName = schemaNode.name;
      // Determine whether ViewInfo should be selected
      const selectedState =
        selectedViewNames.findIndex(viewName => viewName === vwName) === -1
          ? false
          : true;
      // Deteremine whether ViewInfo is an update
      const hasExistingView =
        existingViewNames.findIndex(viewName => viewName === vwName) === -1
          ? false
          : true;
      // Create ViewInfo
      const view: ViewInfo = {
        connectionName: schemaNode.connectionName,
        isUpdate: hasExistingView,
        nodePath: sourcePath,
        selected: selectedState,
        viewName: vwName,
        viewSourceNode: schemaNode,
      };
      viewInfos.push(view);
    }
    // Update path for next level
    sourcePath.push(schemaNode.name);
    // Process this nodes children
    if (schemaNode.children && schemaNode.children.length > 0) {
      for (const childNode of schemaNode.children) {
        generateViewInfos(
          viewInfos,
          childNode,
          sourcePath,
          selectedViewNames,
          existingViewNames
        );
      }
    }
  }
}

/**
 * Recursively flattens the tree structure of SchemaNodes,
 * into an array of ViewInfos
 * @param viewInfos the array of ViewInfos
 * @param schemaNodes the SchemaNode[] array from which the ViewInfo is generated
 * @param nodePath path for current SchemaNode eg ['name0', 'name1', 'name2']
 * @param selectedViewNames names of views which are selected
 * @param existingViewNames names of views which exist (marked as update)
 */
export function generateAllViewInfos(
  viewInfos: ViewInfo[],
  schemaNodes: SchemaNode[],
  nodePath: string[],
  selectedViewNames: string[],
  existingViewNames: string[]
): void {
  if (schemaNodes && schemaNodes.length > 0) {
    // Process each schemaNode
    schemaNodes.map(schemaNode => {
      return generateViewInfos(
        viewInfos,
        schemaNode,
        nodePath,
        selectedViewNames,
        existingViewNames
      );
    });
  }
}

/**
 * Recursively flattens the tree structure of SchemaNodes,
 * into an array of SchemaNodeInfos
 * @param schemaNodeInfos the array of SchemaNodeInfos
 * @param schemaNode the SchemaNode from which the SchemaNodeInfo is generated
 * @param nodePath path for current SchemaNode eg ['sName', 'tName']
 */
export function generateSchemaNodeInfos(
  schemaNodeInfos: SchemaNodeInfo[],
  schemaNode: SchemaNode,
  nodePath: string[]
): void {
  if (schemaNode) {
    // Generate source path from nodePath array
    const sourcePath: string[] = [];
    for (const seg of nodePath) {
      sourcePath.push(seg);
    }

    // Creates SchemaNodeInfo if the SchemaNode is queryable
    if (schemaNode.queryable === true) {
      // Create SchemaNodeInfo
      const view: SchemaNodeInfo = {
        connectionName: schemaNode.connectionName,
        name: schemaNode.name,
        nodePath: sourcePath,
        teiidName: schemaNode.teiidName,
      };
      schemaNodeInfos.push(view);
    }
    // Update path for next level
    if (schemaNode.type !== 'root') {
      sourcePath.push(schemaNode.name);
    }
    // Process this nodes children
    if (schemaNode.children && schemaNode.children.length > 0) {
      for (const childNode of schemaNode.children) {
        generateSchemaNodeInfos(schemaNodeInfos, childNode, sourcePath);
      }
    }
  }
}

/**
 * Generate a ViewDefinition for the supplied info
 * @param schemaNodeInfo the SchemaNodeInfo for the view
 * @param dataVirtName the name of the virtualization
 * @param vwName the name for the view
 * @param vwDescription the (optional) description for the view
 */
export function generateViewDefinition(
  schemaNodeInfo: SchemaNodeInfo[],
  dataVirtName: string,
  vwName: string,
  vwDescription?: string
): ViewDefinition {
  const srcPaths: string[] = loadPaths(schemaNodeInfo);
  return getViewDefinition(
    vwName,
    dataVirtName,
    srcPaths,
    false,
    vwDescription
  );
}

function loadPaths(schemaNodeInfo: SchemaNodeInfo[]): string[] {
  const srcPaths: string[] = [];

  let index = 0;
  schemaNodeInfo.map(
    item =>
      (srcPaths[index++] =
        'schema=' + item.connectionName + '/table=' + item.teiidName)
  );

  return srcPaths;
}

/**
 * Generate a ViewDefinition for the supplied values.
 * @param name the view name
 * @param dataVirtName the name of the virtualization
 * @param srcPaths paths for the sources used in the view
 * @param userDefined specifies if the ddl has been altered from defaults
 * @param description the (optional) view description
 * @param viewDdl the (optional) view DDL
 */
function getViewDefinition(
  name: string,
  dataVirtName: string,
  srcPaths: string[],
  userDefined: boolean,
  description?: string,
  viewDdl?: string
) {
  // View Definition
  const viewDefn: ViewDefinition = {
    dataVirtualizationName: dataVirtName,
    ddl: viewDdl ? viewDdl : '',
    isComplete: true,
    isUserDefined: userDefined,
    keng__description: description ? description : '',
    name,
    sourcePaths: srcPaths,
  };

  return viewDefn;
}

/**
 * Generate array of DvConnections.  Takes the incoming connections and updates the 'options',
 * based on the Virtualization connection status and selection state
 * @param conns the connections
 * @param virtualizationsSourceStatuses the available virtualization sources
 * @param selectedConn name of a selected connection
 */
export function generateDvConnections(
  conns: Connection[],
  virtualizationsSourceStatuses: VirtualizationSourceStatus[],
  selectedConn: string
): Connection[] {
  const dvConns: Connection[] = [];
  for (const conn of conns) {
    const virtSrcStatus = virtualizationsSourceStatuses.find(
      virtStatus => virtStatus.sourceName === conn.name
    );
    // If defined, a corresponding virtualization source was found
    if (virtSrcStatus) {
      let connStatus = DvConnectionStatus.INACTIVE;
      let schemaLoading = String(false);
      let selectionState = String(false);
      // status (ACTIVE, FAILED, INACTIVE)
      switch (virtSrcStatus.schemaState) {
        case 'ACTIVE':
          connStatus = DvConnectionStatus.ACTIVE;
          break;
        case 'FAILED':
          connStatus = DvConnectionStatus.FAILED;
          break;
        case 'MISSING':
          connStatus = DvConnectionStatus.INACTIVE;
          break;
        default:
          break;
      }
      // loading (true/false)
      schemaLoading = String(virtSrcStatus.loading);
      // selection
      if (conn.name === selectedConn) {
        selectionState = String(true);
      }
      conn.options = {
        dvLoading: schemaLoading,
        dvSelected: selectionState,
        dvStatus: connStatus,
      };
      dvConns.push(conn);
    }
  }
  return dvConns;
}

/**
 * Get the Connection DV status.  DV uses the options on a connection to set status
 * @param connection the connection
 */
export function getDvConnectionStatus(conn: Connection): string {
  return conn.options && conn.options.dvStatus
    ? conn.options.dvStatus
    : DvConnectionStatus.INACTIVE;
}

/**
 * Determine if the Connection is selected with the DV wizard.  DV uses the options on a connection to set selection
 * @param connection the connection
 */
export function isDvConnectionSelected(conn: Connection) {
  return conn.options &&
    conn.options.dvSelected &&
    conn.options.dvSelected === String(true)
    ? true
    : false;
}

/**
 * Determine if the Connection is loading.  DV uses the options on a connection to set loading state
 * @param connection the connection
 */
export function isDvConnectionLoading(conn: Connection) {
  return conn.options &&
    conn.options.dvLoading &&
    conn.options.dvLoading === String(true)
    ? true
    : false;
}

/**
 * Get the OData url from the virtualization, if available
 * @param virtualization the RestDataService
 */
export function getOdataUrl(virtualization: RestDataService): string {
  return virtualization.odataHostName
    ? 'https://' + virtualization.odataHostName + '/odata'
    : '';
}

/**
 * Construct the pod build log url from the supplied info
 * @param consoleUrl the console url
 * @param namespace namespace of the DV pod
 * @param publishPodName name of the DV pod
 */
export function getPodLogUrl(
  consoleUrl: string,
  namespace?: string,
  publishPodName?: string
): string {
  return namespace && publishPodName
    ? `${consoleUrl}/project/${namespace}/browse/pods/${publishPodName}?tab=logs`
    : '';
}

/**
 * Get publishing state details for the specified virtualization
 * @param consoleUrl the console url
 * @param virtualization the RestDataService
 */
export function getPublishingDetails(
  consoleUrl: string,
  virtualization: RestDataService
): VirtualizationPublishingDetails {
  // Determine published state
  const publishStepDetails: VirtualizationPublishingDetails = {
    state: virtualization.publishedState,
    stepNumber: 0,
    stepText: '',
    stepTotal: 4,
  };
  switch (virtualization.publishedState) {
    case 'CONFIGURING':
      publishStepDetails.stepNumber = 1;
      publishStepDetails.stepText = 'Configuring';
      break;
    case 'BUILDING':
      publishStepDetails.stepNumber = 2;
      publishStepDetails.stepText = 'Building';
      break;
    case 'DEPLOYING':
      publishStepDetails.stepNumber = 3;
      publishStepDetails.stepText = 'Deploying';
      break;
    case 'RUNNING':
      publishStepDetails.stepNumber = 4;
      publishStepDetails.stepText = 'Published';
      break;
    default:
      break;
  }
  if (virtualization.publishPodName) {
    publishStepDetails.logUrl = getPodLogUrl(
      consoleUrl,
      virtualization.podNamespace,
      virtualization.publishPodName
    );
  }
  return publishStepDetails;
}

/**
 * Generate preview SQL for the specified view definition
 * @param viewDefinition the ViewDefinition
 */
export function getPreviewSql(viewDefinition: ViewDefinition): string {
  return 'SELECT * FROM ' + viewDefinition.name;
}

/**
 * Get rows from the query results
 * @param qResults the query results
 */
export function getQueryRows(qResults: QueryResults): Array<{}> {
  const allRows = qResults.rows ? qResults.rows : [];
  return allRows
    .map(row => row.row)
    .map(row =>
      row.reduce(
        // tslint:disable-next-line: no-shadowed-variable
        (row, r, idx) => ({
          ...row,
          [qResults.columns[idx].name]: r,
        }),
        {}
      )
    );
}

/**
 * Get columns from the query results
 * @param qResults the query results
 */
export function getQueryColumns(qResults: QueryResults): IColumn[] {
  const columns = [];
  if (qResults.columns) {
    for (const col of qResults.columns) {
      columns.push({ id: col.name, label: col.label });
    }
  }
  return columns;
}

/**
 * convert ViewSourceInfo respone into TableColumn array
 * @param sourceInfo the view's source info
 */
export function generateTableColumns(sourceInfo: ViewSourceInfo): ITableInfo[] {
  const tblColumns: ITableInfo[] = [];
  // For each schema, create a TableColumns object for each table and add to array
  sourceInfo.schemas.map(schema => {
    return schema.tables.map(table => {
      const ti = {
        columnNames: table.columns.map(p => p.name),
        name: schema.name + '.' + table.name,
      } as ITableInfo;
      tblColumns.push(ti);
      return ti;
    });
  });
  return tblColumns;
}
