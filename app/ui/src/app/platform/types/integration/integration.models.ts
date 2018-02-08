import { BaseReducerCollectionModel, Action, BaseEntity, Connection, User } from '@syndesis/ui/platform';

export interface Step extends BaseEntity {
  action: Action;
  connection: Connection;
  configuredProperties: {};
  stepKind: string;
}

export type Steps = Array<Step>;

export const DRAFT = 'Draft';
export const PENDING = 'Pending';
export const ACTIVE = 'Active';
export const INACTIVE = 'Inactive';
export const UNDEPLOYED = 'Undeployed';

export type IntegrationStatus = 'Draft' | 'Pending' | 'Active' | 'Inactive' | 'Undeployed';

export interface Integration extends BaseEntity {
  description?: string;
  statusMessage: string;
  token: string;
  steps: Array<Step>;
  gitRepo: string;
  users: Array<User>;
  connections: Array<Connection>;
  userId: string;
  desiredStatus: IntegrationStatus;
  currentStatus: IntegrationStatus;
  stepsDone: Array<string>;
  lastUpdated: string;
  createdDate: string;
  timesUsed: number;
  tags: Array<string>;
  deploymentId?: number;
  version?: number;
}
export type Integrations = Array<Integration>;

export interface IntegrationDeploymentSpec {
  connections: Array<Connection>;
  name: string;
  resources: Array<any>;
  steps: Array<Step>;
  tags: Array<any>;
}

export type IntegrationDeploymentSpecs = Array<IntegrationDeploymentSpec>;

export interface IntegrationDeployment extends BaseEntity {
  createdDate: number;
  lastUpdated: number;
  integrationId: string;
  version: number;
  currentState: IntegrationStatus;
  targetState: IntegrationStatus;
  currentMessage?: string;
  targetMessage?: string;
  spec: IntegrationDeploymentSpec;
  timesUsed: number;
  [attr: string]: any;
}
export type IntegrationDeployments = Array<IntegrationDeployment>;

// this is for the basic filter operation
export interface Op extends BaseEntity {
  label: string;
  operator: string;
}

export type Ops = Array<Op>;

export interface FilterOptions extends BaseEntity {
  paths: Array<string>;
  ops: Array<Op>;
}

export type FilterOptionss = Array<FilterOptions>;

// this is for the logging backend
export interface Activity extends BaseEntity {
  logts?: string;
  at: number;
  pod: string;
  ver: string;
  status: string;
  failed: boolean;
  steps?: ActivityStep[];
  metadata?: any;
}

export interface ActivityStep extends BaseEntity {
  at: number;
  duration?: number;
  failure?: string;
  message?: string[];
  events?: any;
}

// TODO: Remove this TypeScript anti-pattern when the time is right
export function createStep() {
  return {} as Step;
}

// TODO: Remove this TypeScript anti-pattern when the time is right
export function createIntegration() {
  return {} as Integration;
}

export interface IntegrationMetrics {
  id?: string;
  numberOfProcessedMessages: number;
  numberOfErrors: number;
  lastProcessedTimestamp: number; // XXX: Might requrie timestamp parsing through MomentJS
  uptimeInMilliSeconds: number;
}

export interface IntegrationState extends BaseReducerCollectionModel<Integration> {
  metrics: {
    summary: IntegrationMetrics;
    list: Array<IntegrationMetrics>;
  };
}
