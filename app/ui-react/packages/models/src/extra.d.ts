import { Omit } from 'react-router';
import {
  Connector,
  ConnectionBulletinBoard,
  ConnectionOverview,
} from '../dist';
import {
  ConfigurationProperty,
  Connection,
  IntegrationMetricsSummary,
  IntegrationOverview,
  Step,
} from './models';
import { ActionDescriptor } from './models-internal';

/**
 * Extra interfaces and overrides for the swagger generated models
 *
 * ONLY INTERFACES AND TYPES GO IN THIS FILE!
 *
 * NO FUNCTIONS OR CONSTANTS
 *
 */

// this is for the logging backend
export interface Activity {
  logts?: string;
  at: number;
  pod: string;
  ver: string;
  status: string;
  failed: boolean;
  steps?: ActivityStep[];
  metadata?: any;
}

export interface ActivityStep extends Step {
  name: string;
  isFailed: boolean;
  at: number;
  duration?: number;
  failure?: string;
  messages?: string[];
  output?: string;
  events?: any;
}

export interface IApiVersion {
  version: string;
  'commit-id': string;
  'build-id': string;
  camelversion: string;
  camelkruntimeversion: string;
}

export interface IConfigurationProperties {
  [name: string]: IConfigurationProperty;
}

// In reality ConfigurationProperty doesn't advertise everything
export interface IConfigurationProperty extends ConfigurationProperty {
  required?: boolean;
  secret?: boolean;
  [name: string]: any;
}

export interface IConnectionOverview
  extends Omit<ConnectionOverview, 'connector'> {
  connector?: IConnector;
  isConfigRequired: boolean;
  isTechPreview: boolean;
}

export interface IConnector extends Connector {
  isTechPreview: boolean;
}

// Extended connection interface to add support for the 'iconFile' property
export interface IConnectionWithIconFile extends Connection {
  icon?: any;
  iconFile?: File;
}

export interface IIntegrationOverviewWithDraft extends IntegrationOverview {
  isDraft?: boolean;
}

export interface IntegrationWithOverview {
  integration: IIntegrationOverviewWithDraft;
  overview?: IntegrationMetricsSummary;
}

export interface IntegrationWithMonitoring {
  integration: IIntegrationOverviewWithDraft;
  monitoring?: IntegrationMonitoring;
}

export interface IntegrationMonitoring {
  deploymentVersion: number;
  detailedState: {
    value: string;
    currentStep: number;
    totalSteps: number;
  };
  value: string;
  id: string;
  integrationId: string;
  linkType: 'LOGS' | 'EVENTS';
  namespace: string;
  podName: string;
}

export type isVisibleFunction = (
  position: number,
  previous: StepKind[],
  subsequent: StepKind[]
) => boolean;

export interface StepKind extends Step {
  name: string;
  description: string;
  properties: any;
  visible?: isVisibleFunction[];
}

export interface IListResult<T> {
  items?: T[];
  totalCount?: number;
}

/**
 * StringMap allows to model unboundered hash objects
 */
export interface StringMap<T> {
  [key: string]: T;
}

export interface IndexedStep {
  step: Step;
  index: number;
}

export interface ErrorKey {
  displayName: string;
  name: string;
}

export interface ExtendedActionDescriptor extends ActionDescriptor {
  standardizedErrors?: ErrorKey[];
}

/**
 * The error response object that can come back from API responses, not sure why it's not in the generated model
 */
export interface ErrorResponse {
  errorCode?: number;
  userMsg: string;
  developerMsg?: string;
}

/**
 * It looks like when saving an integration we get a different object than the above
 */
export interface IntegrationSaveErrorResponse {
  error: string;
  message: string;
  property: string;
}
