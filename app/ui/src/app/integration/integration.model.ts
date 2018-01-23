import { Action, BaseEntity, Connection, User } from '@syndesis/ui/model';

export interface Step extends BaseEntity {
  action: Action;
  connection: Connection;
  name: string;
  configuredProperties: {};
  stepKind: string;
  id: string;
}
export type Steps = Array<Step>;

export interface Integration extends BaseEntity {
  configuration: string;
  description: string;
  deployedRevisionId: number;
  revisions: Array<IntegrationRevision>;
  statusMessage: string;
  token: string;
  steps: Array<Step>;
  draftRevision: IntegrationRevision;
  gitRepo: string;
  users: Array<User>;
  connections: Array<Connection>;
  userId: string;
  desiredStatus: 'Draft' | 'Pending' | 'Active' | 'Inactive' | 'Undeployed';
  currentStatus: 'Draft' | 'Pending' | 'Active' | 'Inactive' | 'Undeployed';
  stepsDone: Array<string>;
  lastUpdated: string;
  createdDate: string;
  timesUsed: number;
  integrationTemplateId: string;
  id: string;
  tags: Array<string>;
  name: string;
}
export type Integrations = Array<Integration>;

export interface IntegrationRevision extends BaseEntity {
  currentMessage: string;
  parentVersion: number;
  targetState:
    | 'Draft'
    | 'Active'
    | 'Inactive'
    | 'Undeployed'
    | 'Error'
    | 'Pending';
  targetMessage: string;
  version: number;
  spec: IntegrationRevisionSpec;
  currentState:
    | 'Draft'
    | 'Active'
    | 'Inactive'
    | 'Undeployed'
    | 'Error'
    | 'Pending';
}
export type IntegrationRevisions = Array<IntegrationRevision>;

export interface IntegrationRevisionSpec extends BaseEntity {
  configuration: string;
  steps: Array<Step>;
  connections: Array<Connection>;
}
export type IntegrationRevisionSpecs = Array<IntegrationRevisionSpec>;

export function createStep() {
    return <Step>{
      action: undefined,
      connection: undefined,
      name: undefined,
      configuredProperties: undefined,
      stepKind: undefined,
      id: undefined
    };
  }

export function createIntegration() {
  return <Integration>{
    configuration: undefined,
    description: undefined,
    deployedRevisionId: undefined,
    revisions: undefined,
    statusMessage: undefined,
    token: undefined,
    steps: undefined,
    draftRevision: undefined,
    gitRepo: undefined,
    users: undefined,
    connections: undefined,
    userId: undefined,
    desiredStatus: undefined,
    currentStatus: undefined,
    stepsDone: undefined,
    lastUpdated: undefined,
    createdDate: undefined,
    timesUsed: undefined,
    integrationTemplateId: undefined,
    id: undefined,
    tags: undefined,
    name: undefined
  };
}

export function createIntegrationRevision() {
  return <IntegrationRevision>{
    currentMessage: undefined,
    parentVersion: undefined,
    targetState: undefined,
    targetMessage: undefined,
    version: undefined,
    spec: undefined,
    currentState: undefined
  };
}

export function createIntegrationRevisionSpec() {
  return <IntegrationRevisionSpec>{
    configuration: undefined,
    steps: undefined,
    connections: undefined
  };
}
