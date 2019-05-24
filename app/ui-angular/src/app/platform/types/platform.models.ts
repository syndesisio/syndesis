import { DynamicFormControlRelationGroup } from '@ng-dynamic-forms/core';
import { Connection } from './connection/connection.models';
import { Step } from './integration/integration.models';
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
  data?: any;
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

export interface WithId {
  id?: string;
}

export interface WithMetadata {
  metadata?: StringMap<string>;
}

export interface BaseEntity extends WithId {
  kind?: string;
  name?: string;
}

export enum MessageLevel {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR'
}

export enum MessageCode {
  SYNDESIS000 = 'SYNDESIS000', // generic message
  SYNDESIS001 = 'SYNDESIS001', // One or more properties have been updated
  SYNDESIS002 = 'SYNDESIS002', // One or more properties have been added or removed
  SYNDESIS003 = 'SYNDESIS003', // Connector has been deleted
  SYNDESIS004 = 'SYNDESIS004', // Extension has been deleted
  SYNDESIS005 = 'SYNDESIS005', // Action has been deleted
  SYNDESIS006 = 'SYNDESIS006', // One or more required properties is not set
  SYNDESIS007 = 'SYNDESIS007', // Secrets update needed
  SYNDESIS008 = 'SYNDESIS008' // Validation Error
}

export interface LeveledMessage {
  level?: MessageLevel;
  code?: MessageCode;
  message?: string;
}

export interface WithLeveledMessages {
  messages?: Array<LeveledMessage>;
}

export interface WithModificationTimestamps {
  createdAt?: number;
  updatedAt?: number;
}

export interface ConfigurationProperty extends BaseEntity {
  javaType: string;
  type: string;
  multiple: boolean;
  defaultValue: string;
  displayName: string;
  description: string;
  labelHint: string;
  controlHint: string;
  placeholder: string;
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

export interface BulletinBoard
  extends WithId,
    WithMetadata,
    WithLeveledMessages,
    WithModificationTimestamps {}

export enum DataShapeKinds {
  ANY = 'any',
  JAVA = 'java',
  JSON_SCHEMA = 'json-schema',
  JSON_INSTANCE = 'json-instance',
  XML_SCHEMA = 'xml-schema',
  XML_SCHEMA_INSPECTED = 'xml-schema-inspected',
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
  pattern: 'From' | 'Pipe' | 'To';
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

export type StepOrConnection = Connection | Step;
