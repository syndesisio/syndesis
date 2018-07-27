import {
  BaseReducerCollectionModel,
  Action,
  BaseEntity,
  Connection,
  key,
  WithLeveledMessages,
  StringMap
} from '@syndesis/ui/platform';

export class Step implements BaseEntity {
  id?: string;
  kind?: string;
  name?: string;
  action: Action;
  connection: Connection;
  configuredProperties: {};
  stepKind?: string;
  metadata?: StringMap<any>;

  constructor() {
    this.id = key();
  }
}

export type Steps = Array<Step>;

export const PENDING = 'Pending';
export const PUBLISHED = 'Published';
export const UNPUBLISHED = 'Unpublished';
export const ERROR = 'Error';

export type IntegrationStatus =
  | 'Pending'
  | 'Published'
  | 'Unpublished'
  | 'Error';

export interface IntegrationOverview extends BaseEntity, WithLeveledMessages {
  version?: number;
  tags: Array<string>;
  description?: string;
  isDraft: boolean;
  deployments?: Array<DeploymentOverview>;
  currentState: IntegrationStatus;
  targetState: IntegrationStatus;
  statusMessage?: string;
  deploymentVersion?: number;
}

export type IntegrationOverviews = Array<IntegrationOverview>;

export interface Integration extends IntegrationOverview {
  steps: Array<Step>;
  connections: Array<Connection>;
  userId: string;
  desiredState: IntegrationStatus;
  stepsDone: Array<string>;
  lastUpdated: string;
  createdDate: string;
  timesUsed: number;
  deploymentId?: number;
  updatedAt: number;
  createdAt: number;
  url: string;
  statusDetail?: IntegrationStatusDetail;
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

export interface DeploymentOverview extends BaseEntity {
  version: number;
  currentState: IntegrationStatus;
  targetState: IntegrationStatus;
  createdAt: number;
  integrationVersion: number;
}

export enum ConsoleLinkType {
  Events = 'EVENTS',
  Logs = 'LOGS'
}

export enum DetailedStatus {
  Assembling = 'ASSEMBLING',
  Building = 'BUILDING',
  Deploying = 'DEPLOYNG',
  Starting = 'STARTING'
}

export interface DetailedState {
  value: DetailedStatus;
  currentStep: number;
  totalSteps: number;
}

export interface IntegrationStatusDetail {
  detailedState: DetailedState;
  linkType?: ConsoleLinkType;
  logsUrl?: string;
  eventsUrl?: string;
  id: string;
  integrationId: string;
  deploymentVersion: number;
  namespace: string;
  podName: string;
}

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
  isFailed: boolean;
  failure?: string;
  messages?: string[];
  output?: string;
  events?: any;
}

export function createStep(): Step {
  return <Step>{ id: key() };
}

export function createConnectionStep(): Step {
  const step = createStep();
  step.stepKind = 'endpoint';
  return step;
}

// TODO: Remove this TypeScript anti-pattern when the time is right
export function createIntegration() {
  return {} as Integration;
}

export interface IntegrationMetrics {
  id?: string;
  messages: number;
  errors: number;
  start: number;
  lastProcessed: number;
}

export interface IntegrationState
  extends BaseReducerCollectionModel<Integration> {
  metrics: {
    summary: IntegrationMetrics;
    list: Array<IntegrationMetrics>;
  };
}
