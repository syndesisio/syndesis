import {
  Connection,
  ProjectedColumn,
  SchemaNode,
  ViewDefinition,
  ViewEditorState,
  ViewInfo,
  VirtualizationSourceStatus,
} from '@syndesis/models';

export enum DvConnectionStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum DvConnectionSelection {
  SELECTED = 'SELECTED',
  NOTSELECTED = 'NOTSELECTED',
}

/**
 * Recursively flattens the tree structure of SchemaNodes,
 * into an array of ViewInfos
 * @param viewInfos the array of ViewInfos
 * @param schemaNode the SchemaNode from which the ViewInfo is generated
 * @param nodePath path for current SchemaNode
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
      const vwName = schemaNode.connectionName + '_' + schemaNode.name;
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
 * Generates ViewEditorStates for the supplied ViewInfos
 * @param serviceVdbName the name of the virtualization vdb
 * @param viewInfos the array of ViewInfos
 */
export function generateViewEditorStates(
  serviceVdbName: string,
  viewInfos: ViewInfo[]
): ViewEditorState[] {
  const editorStates: ViewEditorState[] = [];
  for (const viewInfo of viewInfos) {
    const srcPaths: string[] = [];
    const path =
      'connection=' +
      viewInfo.connectionName +
      '/' +
      viewInfo.viewSourceNode.path;
    srcPaths.push(path);

    // All columns are projected
    const projCols: ProjectedColumn[] = [];
    const projCol: ProjectedColumn = {
      name: 'ALL',
      selected: true,
      type: 'ALL',
    };
    projCols.push(projCol);

    // View Definition
    // TODO: need to supply the description here instead of generate
    const viewDefn: ViewDefinition = {
      compositions: [],
      isComplete: true,
      keng__description: viewInfo.viewName + ' description',
      projectedColumns: projCols,
      sourcePaths: srcPaths,
      viewName: viewInfo.viewName,
    };

    // ViewEditorState which is supplied to the user profile
    const editorState: ViewEditorState = {
      id: serviceVdbName + '.' + viewInfo.viewName,
      viewDefinition: viewDefn,
    };
    editorStates.push(editorState);
  }
  return editorStates;
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
    let connStatus = DvConnectionStatus.INACTIVE;
    const virtSrcStatus = virtualizationsSourceStatuses.find(
      virtStatus => virtStatus.sourceName === conn.name
    );
    if (
      virtSrcStatus &&
      virtSrcStatus.vdbState === 'ACTIVE' &&
      virtSrcStatus.schemaState === 'ACTIVE'
    ) {
      connStatus = DvConnectionStatus.ACTIVE;
    }

    let selectionState = DvConnectionSelection.NOTSELECTED;
    if (conn.name === selectedConn) {
      selectionState = DvConnectionSelection.SELECTED;
    }
    conn.options = { dvStatus: connStatus, dvSelected: selectionState };
    dvConns.push(conn);
  }
  return dvConns;
}

/**
 * Get the Connection DV status.  DV uses the options on a connection to set status
 * @param connection the connection
 */
export function getDvConnectionStatus(conn: Connection): string {
  let dvState: string = DvConnectionStatus.INACTIVE;
  if (conn.options && conn.options.dvStatus) {
    dvState = conn.options.dvStatus;
  }
  return dvState;
}

/**
 * Determine if the Connection is selected with the DV wizard.  DV uses the options on a connection to set selection
 * @param connection the connection
 */
export function isDvConnectionSelected(conn: Connection) {
  let isSelected = false;
  if (
    conn.options &&
    conn.options.dvSelected &&
    conn.options.dvSelected === DvConnectionSelection.SELECTED
  ) {
    isSelected = true;
  }
  return isSelected;
}
