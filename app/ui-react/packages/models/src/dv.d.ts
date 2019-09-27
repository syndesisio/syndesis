// TODO remove when these values are advertised by the swagger
export interface RestDataService {
  empty: boolean;
  id: string;
  keng__id: string;
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
  serviceViewModel: string;
  tko__description: string;
  usedBy: string[];
}

export interface SchemaNode {
  name: string;
  teiidName: string;
  type: string;
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
  name: string;
  nodePath: string[];
  teiidName: string;
}

export interface VirtualizationSourceStatus {
  errors: string[];
  id: string;
  loading: boolean;
  schemaState: 'ACTIVE' | 'MISSING' | 'FAILED';
  sourceName: string;
}

export interface ViewDefinitionDescriptor {
  id: string;
  name: string;
  description: string;
}

export interface ViewDefinition {
  id?: string;
  name: string;
  dataVirtualizationName: string;
  keng__description: string;
  isComplete: boolean;
  isUserDefined: boolean;
  sourcePaths: string[];
  ddl?: string;
  createdAt?: number;
  modifiedAt?: number;
  version?: number;
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

export interface ViewSourceInfo {
  schemas: SourceSchema[];
  viewName: string;
}

export interface SourceSchema {
  name: string;
  tables: SourceTable[];
}

export interface SourceTable {
  name: string;
  columns: SourceColumn[];
}

export interface SourceColumn {
  name: string;
  datatype: string;
}

export interface TableColumns {
  name: string;
  columnNames: string[];
}

export interface ViewDefinitionStatus {
  status: string;
  message: string;
}

export interface ImportSourcesStatus {
  Title: string;
}

export interface ImportSources {
  tables: string[];
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

export interface TeiidStatus {
  Information: {
    error?: string;
    log?: string;
    Publishing?: string;
    'Build Status'?:
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
    'Build Status Message'?: string;
    'OpenShift Name'?: string;
    [name: string]: any;
  };
  Title?: string;
}

export interface BuildStatus {
  build_name?: string;
  build_status?:
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
  build_status_message?: string;
  dataVirtualizationName?: string;
  deployment_name?: string;
  last_updated?: number;
  namespace?: string;
  openShiftName: string;
  routes?: {
    [name: string]: any;
  };
  usedBy?: string[];
}
