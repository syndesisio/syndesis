// TODO remove when these values are advertised by the swagger
export interface Virtualization {
  deployedMessage?: string;
  deployedState:
    | 'NOTFOUND'
    | 'DEPLOYING'
    | 'FAILED'
    | 'RUNNING';
  deployedRevision?: number;
  empty: boolean;
  id: string;
  modified: boolean;
  name: string;
  odataHostName?: string;
  podNamespace?: string;
  publishPodName?: string;
  publishedMessage?: string;
  publishedState:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'COMPLETE'
    | 'DELETE_SUBMITTED'
    | 'DELETE_REQUEUE'
    | 'DELETE_DONE'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  publishedRevision?: number;
  serviceViewModel: string;
  description: string;
  secured: boolean;
  usedBy: string[];
}

export interface VirtualizationEdition {
  id: string;
  description: string;
  revision: number;
  dataVirtualizationName: string;
  createdAt: string;
}

export interface VirtualizationMetrics {
  startedAt: string;
  sessions: number;
  requestCount: number;
  resultSetCacheHitRatio: number;
}

export interface RoleInfo {
  operation: 'GRANT' | 'REVOKE';
  tablePrivileges: TablePrivilege[];
}

export interface TablePrivilege {
  grantPrivileges: string[];
  roleName: string | undefined;
  viewDefinitionIds: string[];
}

export interface SchemaNode {
  name: string;
  teiidName: string;
  type: string;
  connectionName: string;
  queryable: boolean;
  children: SchemaNode[];
}

export interface TableInfo {
  connectionName: string;
  tableName: string;
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
  teiidName: string;
  lastLoad: number;
}

export interface DVStatusObj {
  exposeVia3scale: string;
  ssoConfigured: string;
}

export interface DVStatus {
  attributes: DVStatusObj;
}

export interface ViewDefinitionDescriptor {
  id: string;
  name: string;
  description: string;
  valid: boolean;
  tablePrivileges: TablePrivilege[]
}

export interface ViewDefinition {
  id?: string;
  name: string;
  dataVirtualizationName: string;
  description: string;
  status: 'SUCCESS' | 'ERROR';
  message: string;
  complete: boolean;
  userDefined: boolean;
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

export interface ConnectionTable {
  name: string;
  tables: SourceTable[];
}

export interface ViewDefinitionStatus {
  status: string;
  message: string;
}

export interface ImportSourcesStatus {
  title: string;
}

export interface ImportSources {
  tables: string[];
}

export interface VirtualizationPublishingDetails {
  logUrl?: string;
  modified: boolean;
  state:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'COMPLETE'
    | 'DELETE_SUBMITTED'
    | 'DELETE_REQUEUE'
    | 'DELETE_DONE'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  stepNumber: number;
  stepText: string;
  stepTotal: number;
  version?: number;
}

export interface TeiidStatus {
  attributes: {
    error?: string;
    log?: string;
    publishing?: string;
    'Build Status'?:
      | 'BUILDING'
      | 'CANCELLED'
      | 'CONFIGURING'
      | 'COMPLETE'
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
  title?: string;
}

export interface BuildStatus {
  name?: string;
  status?:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
    | 'COMPLETE'
    | 'DELETE_SUBMITTED'
    | 'DELETE_REQUEUE'
    | 'DELETE_DONE'
    | 'DEPLOYING'
    | 'FAILED'
    | 'NOTFOUND'
    | 'RUNNING'
    | 'SUBMITTED';
  statusMessage?: string;
  dataVirtualizationName?: string;
  deploymentName?: string;
  lastUpdated?: number;
  namespace?: string;
  openShiftName: string;
  routes?: {
    [name: string]: any;
  };
  usedBy?: string[];
}
