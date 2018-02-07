import { Action, BaseEntity, Connection, User } from '@syndesis/ui/platform';

export interface Step extends BaseEntity {
  action: Action;
  connection: Connection;
  name: string;
  configuredProperties: {};
  stepKind: string;
  id: string;
}
export type Steps = Array<Step>;

export const DRAFT = 'Draft';
export const PENDING = 'Pending';
export const PUBLISHED = 'Published';
export const UNPUBLISHED = 'Unpublished';

export type IntegrationState = 'Pending' | 'Published' | 'Unpublished' | 'Error';

export interface Integration extends BaseEntity {
  description?: string;
  steps: Array<Step>;
  connections: Array<Connection>;
  stepsDone: Array<string>;
  updatedAt: number;
  createdAt: number;
  id: string;
  tags: Array<string>;
  name: string;
  deploymentVersion?: number;
  version?: number;
}
export type Integrations = Array<Integration>;

export interface IntegrationDeployment extends BaseEntity {
  version: number;
  updatedAt: number;
  createdAt: number;
  integrationId: string;
  currentState: IntegrationState;
  targetState: IntegrationState;
  statusMessage?: string;
  spec: Integration;
  [attr: string]: any;
}
export type IntegrationDeployments = Array<IntegrationDeployment>;

export interface IntegrationOverview extends BaseEntity {
  version: number;
  name: string;
  tags: Array<string>;
  description?: string;
  draft: boolean;
  deployments?: Array<DeploymentOverview>;
  currentState: IntegrationState;
  targetState: IntegrationState;
  statusMessage?: string;
}
export type IntegrationOverviews = Array<IntegrationOverview>;

export interface DeploymentOverview extends BaseEntity {
  version: number;
  currentState: IntegrationState;
  targetState: IntegrationState;
  createdAt: number;
  integrationVersion: number;
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
  failure?: string;
  message?: string[];
  events?: any;
}

export function createStep() {
  return {} as Step;
}

export function createIntegration() {
  return {} as Integration;
}
