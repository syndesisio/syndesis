import { DynamicFormControlRelationGroup } from '@ng-dynamic-forms/core';
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
export interface ConfigurationProperty extends BaseEntity {
  javaType: string;
  type: string;
  defaultValue: string;
  displayName: string;
  description: string;
  group: string;
  required: boolean;
  secret: boolean;
  label: string;
  relation: DynamicFormControlRelationGroup[];
  order: number;
  enum: Array<PropertyValue>;
  componentProperty: boolean;
  deprecated: boolean;
  tags: Array<string>;
}
export type ConfigurationProperties = Array<ConfigurationProperty>;

export interface PropertyValue extends BaseEntity {
  value: string;
  label: string;
}
export type PropertyValues = Array<PropertyValue>;

export interface ConfiguredConfigurationProperty extends ConfigurationProperty {
  value: any;
  rows?: number;
  cols?: number;
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
  metadata: StringMap<string>;
}
export type DataShapes = Array<DataShape>;

export interface Action extends BaseEntity {
  actionType: string;
  pattern: 'From' | 'To';
  descriptor: ActionDescriptor;
  connectorId: string;
  description: string;
  tags: Array<string>;
}

export type Actions = Array<Action>;

export interface ActionDescriptor extends BaseEntity {
  componentScheme: string;
  configuredProperties: StringMap<string>;
  propertyDefinitionSteps: Array<ActionDescriptorStep>;
  inputDataShape: DataShape;
  outputDataShape: DataShape;
}

export type ActionDescriptors = Array<ActionDescriptor>;

export interface ActionDescriptorStep extends BaseEntity {
  description: string;
  configuredProperties: StringMap<string>;
  properties: StringMap<ConfigurationProperty>;
}

export type ActionDescriptorSteps = Array<ActionDescriptorStep>;

export interface ListResultAction extends BaseEntity {
  items: Array<Action>;
  totalCount: number;
}

export type ListResultActions = Array<ListResultAction>;
