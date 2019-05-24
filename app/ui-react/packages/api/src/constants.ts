// Enum for the LeveledMessage level field
export enum MessageLevel {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

// Data shape kind enum when working with the DataShape type
export enum DataShapeKinds {
  ANY = 'any',
  JAVA = 'java',
  JSON_SCHEMA = 'json-schema',
  JSON_INSTANCE = 'json-instance',
  XML_SCHEMA = 'xml-schema',
  XML_SCHEMA_INSPECTED = 'xml-schema-inspected',
  XML_INSTANCE = 'xml-instance',
  NONE = 'none',
}

// These are message codes that we know about
export enum MessageCode {
  SYNDESIS000 = 'SYNDESIS000', // generic message
  SYNDESIS001 = 'SYNDESIS001', // One or more properties have been updated
  SYNDESIS002 = 'SYNDESIS002', // One or more properties have been added or removed
  SYNDESIS003 = 'SYNDESIS003', // Connector has been deleted
  SYNDESIS004 = 'SYNDESIS004', // Extension has been deleted
  SYNDESIS005 = 'SYNDESIS005', // Action has been deleted
  SYNDESIS006 = 'SYNDESIS006', // One or more required properties is not set
  SYNDESIS007 = 'SYNDESIS007', // Secrets update needed
  SYNDESIS008 = 'SYNDESIS008', // Validation Error
}

// Integration status types and consts
export const PENDING = 'Pending';
export const PUBLISHED = 'Published';
export const UNPUBLISHED = 'Unpublished';
export const ERROR = 'Error';

export type IntegrationStatus =
  | 'Pending'
  | 'Published'
  | 'Unpublished'
  | 'Error';

export enum IntegrationType {
  SingleFlow = 'SingleFlow',
  ApiProvider = 'ApiProvider',
  MultiFlow = 'MultiFlow',
}

// These types are for the integration detailed state
// TODO: this should come from the swagger but it's missing
export enum ConsoleLinkType {
  Events = 'EVENTS',
  Logs = 'LOGS',
}

export enum DetailedStatus {
  Assembling = 'ASSEMBLING',
  Building = 'BUILDING',
  Deploying = 'DEPLOYING',
  Starting = 'STARTING',
}

// Special sekret connection metadata keys
export const HIDE_FROM_STEP_SELECT = 'hide-from-step-select';
export const HIDE_FROM_CONNECTION_PAGES = 'hide-from-connection-pages';

// stuff used by the ui to compute the StepKinds
export const EXTENSION = 'extension';
export const ENDPOINT = 'endpoint';
export const CONNECTION = ENDPOINT;
export const DATA_MAPPER = 'mapper';
export const BASIC_FILTER = 'ruleFilter';
export const ADVANCED_FILTER = 'expressionFilter';
export const STORE_DATA = 'storeData';
export const SET_DATA = 'setData';
export const CALL_ROUTE = 'callRoute';
export const CONDITIONAL_PROCESSING = 'conditionalProcessing';
export const SPLIT = 'split';
export const AGGREGATE = 'aggregate';
export const LOG = 'log';
export const TEMPLATE = 'template';
export const API_PROVIDER = 'api-provider';
export const CONNECTOR = 'connector';
