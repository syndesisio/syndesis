// Enum for the LeveledMessage level field
import { Integration } from '@syndesis/models';

export enum MessageLevel {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

export const NEW_INTEGRATION_ID = 'new-integration';

export const NEW_INTEGRATION = {
  id: NEW_INTEGRATION_ID,
  name: '',
  tags: [],
} as Integration;

export type DataShapeKindType =
  | 'ANY'
  | 'JAVA'
  | 'JSON_SCHEMA'
  | 'JSON_INSTANCE'
  | 'XML_SCHEMA'
  | 'XML_SCHEMA_INSPECTED'
  | 'XML_INSTANCE'
  | 'NONE';

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

export enum FlowType {
  PRIMARY = 'PRIMARY',
  API_PROVIDER = 'API_PROVIDER',
  ALTERNATE = 'ALTERNATE',
}

export enum FlowKind {
  CONDITIONAL = 'conditional',
  DEFAULT = 'default',
}

export const FLOW_KIND_METADATA_KEY = 'kind';
export const EXCERPT_METADATA_KEY = 'excerpt';
export const STEP_ID_METADATA_KEY = 'stepId';
export const PRIMARY_FLOW_ID_METADATA_KEY = 'primaryFlowId';

// Special action IDs
export const API_PROVIDER_END_ACTION_ID = 'io.syndesis:api-provider-end';
export const FLOW_START_ACTION_ID = 'io.syndesis:flow-start';
export const FLOW_END_ACTION_ID = 'io.syndesis:flow-end';

// Special sekret connection metadata keys
export const HIDE_FROM_STEP_SELECT = 'hide-from-step-select';
export const HIDE_FROM_CONNECTION_PAGES = 'hide-from-connection-pages';

// stuff used by the ui to compute the StepKinds
export const EXTENSION = 'extension';
export const ENDPOINT = 'endpoint';
export const FLOW = 'flow';
export const CONNECTION = ENDPOINT;
export const DATA_MAPPER = 'mapper';
export const BASIC_FILTER = 'ruleFilter';
export const ADVANCED_FILTER = 'expressionFilter';
export const SPLIT = 'split';
export const AGGREGATE = 'aggregate';
export const LOG = 'log';
export const TEMPLATE = 'template';
export const API_PROVIDER = 'api-provider';
export const CONNECTOR = 'connector';
export const CHOICE = 'choice';
