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
  publishedState:
    | 'BUILDING'
    | 'CANCELLED'
    | 'CONFIGURING'
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
}

export interface ViewEditorState {
  id: string;
  viewDefinition: ViewDefinition;
}
