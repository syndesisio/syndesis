import {
  ConfigurationProperty,
  Connection,
  IntegrationMetricsSummary,
  IntegrationOverview,
  Step,
} from './models';

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

export interface StepKind extends Step {
  name: string;
  description: string;
  properties: any;
  visible?: (position: number, previous: Step[], subsequent: Step[]) => boolean;
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
