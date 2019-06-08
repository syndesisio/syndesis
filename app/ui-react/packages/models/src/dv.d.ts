// TODO remove when these values are advertised by the swagger
export interface RestDataService {
  connections: number;
  drivers: number;
  keng___links: [];
  keng__baseUri: string;
  keng__dataPath: string;
  keng__hasChildren: boolean;
  keng__id: string;
  keng__kType: string;
  odataHostName?: string;
  podNamespace?: string;
  publishPodName?: string;
  publishedState:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'DELETE_SUBMITTED'
    | 'DELETE_REQUEUE'
    | 'DELETE_DONE'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  serviceVdbName: string;
  serviceVdbVersion: string;
  serviceViewDefinitions: string[];
  serviceViewModel: string;
  tko__description: string;
}

export interface RestViewDefinition {
  keng__baseUri: string;
  keng__id: string;
  keng__dataPath: string;
  keng__kType: string;
  keng__hasChildren: boolean;
  keng___links: [];
}

export interface RestVdbModel {
  keng__baseUri: string;
  keng__id: string;
  keng__dataPath: string;
  keng__kType: string;
  keng__hasChildren: boolean;
  mmcore__modelType: string;
  vdb__visible: boolean;
  vdb__metadataType: string;
  keng__ddl: string;
}

export interface SchemaNode {
  name: string;
  type: string;
  path: string;
  connectionName: string;
  queryable: boolean;
  children: SchemaNode[];
}

export interface ViewInfo {
  connectionName: string;
  isUpdate: boolean;
  nodePath: string[];
  selected: boolean;
  viewName: string;
  viewDescription?: string;
  viewSourceNode: SchemaNode;
}

export interface SchemaNodeInfo {
  connectionName: string;
  sourceName: string;
  sourcePath: string;
}

export interface VirtualizationSourceStatus {
  sourceName: string;
  hasTeiidSource: boolean;
  vdbState: 'ACTIVE' | 'MISSING' | 'LOADING' | 'FAILED';
  schemaState: 'ACTIVE' | 'MISSING' | 'LOADING' | 'FAILED';
  errors: string[];
  vdbName?: string;
  schemaVdbName?: string;
  schemaModelName?: string;
}

export interface ProjectedColumn {
  name: string;
  type: string;
  selected: boolean;
}

export interface ViewDefinition {
  isComplete: boolean;
  viewName: string;
  keng__description: string;
  sourcePaths: string[];
  compositions: string[];
  projectedColumns: ProjectedColumn[];
  ddl?: string;
}

export interface ViewEditorState {
  id: string;
  viewDefinition: ViewDefinition;
}

export interface ColumnData {
  name: string;
  label: string;
  type: string;
}

export interface RowData {
  row: string[];
}

export interface QueryResults {
  columns: ColumnData[];
  rows: RowData[];
}

export interface ViewDefinitionStatus {
  status: string;
  message: string;
}

export interface VirtualizationPublishingDetails {
  state:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'DELETE_SUBMITTED'
    | 'DELETE_REQUEUE'
    | 'DELETE_DONE'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  logUrl?: string;
  stepNumber: number;
  stepText: string;
  stepTotal: number;
}
