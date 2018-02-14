/**
 * StringMap allows to model unboundered hash objects
 */
export interface StringMap<T> {
  [key: string]: T;
}

/**
 * FileMap allows to model FormData objects containing files mapped to named keys
 */
export interface FileMap {
  [key: string]: File;
}

/**
 * A convenience model to map internal UI errors, either derived from Http sync operations
 * or any other state handling actions that might throw an exception.
 */
export interface ActionReducerError {
  errorCode?: any;
  message: string;
  debugMessage?: string;
  status?: number;
  statusText?: string;
}

/**
 * BaseReducerModel must be applied to all interfaces modelling
 * a NgRX-managed slice of state featuring its own reducer.
 */
export interface BaseReducerModel {
  loading?: boolean;
  loaded?: boolean;
  hasErrors?: boolean;
  errors?: Array<ActionReducerError>;
}

/**
 * BaseReducerCollectionModel is a specialized model that extends BaseReducerModel
 * and adds specific properties for handling state stores that manage collections
 * subject to be updated through CRUD actions.
 */
export interface BaseReducerCollectionModel<T> extends BaseReducerModel {
  collection: Array<T>;
  inserted?: T;
  deleted?: T;
}

/**
 * Common interface for modelling requests requiring several steps for accomplishment
 * which require additional flags to track progress level and success
 */
export interface BaseRequestModel {
  isRequested?: boolean;
  isOK?: boolean;
  isComplete?: boolean;
}

/**
 * Global interfaces for modelling all kind of core Syndesis entities
 * TODO: Document each one in more detail
 */
export interface BaseEntity {
  id?: string;
  kind?: string;
  name?: string;
}

export enum DataShapeKinds {
  ANY = 'any',
  JAVA = 'java',
  JSON_SCHEMA = 'json-schema',
  JSON_INSTANCE = 'json-instance',
  XML_SCHEMA = 'xml-schema',
  XML_INSTANCE = 'xml-instance',
  NONE = 'none'
}

export interface DataShape {
  id?: string;
  kind: DataShapeKinds;
  specification: string;
  exemplar: Array<string>;
  type: string;
  name: string;
  description: string;
}
export type DataShapes = Array<DataShape>;

export interface Action extends BaseEntity {
  actionType: string;
  pattern: 'From' | 'To';
  // TODO migrate this to ActionDescriptor
  descriptor: ActionDefinition;
  connectorId: string;
  description: string;
  tags: Array<string>;
}

export type Actions = Array<Action>;

export interface ActionDescriptor extends BaseEntity {
  propertyDefinitionSteps: Array<ActionDescriptorStep>;
  inputDataShape: DataShape;
  outputDataShape: DataShape;
}

export type ActionDescriptors = Array<ActionDescriptor>;

export interface ActionDescriptorStep extends BaseEntity {
  description: string;
  configuredProperties: {};
  properties: {};
}

export type ActionDescriptorSteps = Array<ActionDescriptorStep>;

// TODO deprecate should be ActionDescriptor
export interface ActionDefinition extends BaseEntity {
  camelConnectorGAV: string;
  camelConnectorPrefix: string;
  outputDataShape: DataShape;
  inputDataShape: DataShape;
  propertyDefinitionSteps: Array<ActionDefinitionStep>;
}

export type ActionDefinitions = Array<ActionDefinition>;

// TODO deprecate should be ActionDescriptorStep
export interface ActionDefinitionStep extends BaseEntity {
  description: string;
  properties: {};
  configuredProperties: {};
}

export type ActionDefinitionSteps = Array<ActionDefinitionStep>;

export interface ListResultAction extends BaseEntity {
  items: Array<Action>;
  totalCount: number;
}

export type ListResultActions = Array<ListResultAction>;
