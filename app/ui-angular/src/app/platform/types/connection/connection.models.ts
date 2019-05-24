import {
  BaseEntity,
  User,
  Action,
  StringMap,
  ConfigurationProperty,
  BulletinBoard
} from '@syndesis/ui/platform';

// these are related to oauth enabled connections
export interface AcquisitionMethod extends BaseEntity {
  icon: string;
  type: 'OAUTH1' | 'OAUTH2';
  description: string;
  label: string;
}

export type AcquisitionMethods = Array<AcquisitionMethod>;

export interface AcquisitionRequest extends BaseEntity {
  returnUrl: string;
}

export type AcquisitionRequests = Array<AcquisitionRequest>;

export interface Result extends BaseEntity {
  scope: 'PARAMETERS' | 'CONNECTIVITY';
  errors: Array<Error>;
  status: 'OK' | 'ERROR' | 'UNSUPPORTED';
}

export type Results = Array<Result>;

export interface Connector extends BaseEntity {
  icon: string;
  properties: StringMap<ConfigurationProperty>;
  actions: Array<Action>;
  connectorGroupId: string;
  configuredProperties: StringMap<string>;
  description: string;
  connectorGroup: BaseEntity;
  tags: Array<string>;
  metadata?: StringMap<any>;
}

export interface Organization extends BaseEntity {
  environments: Array<BaseEntity>;
  users: Array<User>;
}

export type Organizations = Array<Organization>;

export interface ConnectionBulletinBoard extends BaseEntity, BulletinBoard {
  notices: number;
  warnings: number;
  errors: number;
  targetResourceId?: string;
}

export interface Connection extends BaseEntity {
  icon: string;
  board: ConnectionBulletinBoard;
  organization: Organization;
  configuredProperties: StringMap<string>;
  organizationId: string;
  connectorId: string;
  options: {};
  description: string;
  connector: Connector;
  derived: boolean;
  userId: string;
  lastUpdated: string;
  createdDate: string;
  tags: Array<string>;
  uses?: number;
  metadata?: StringMap<any>;
}

export type Environments = Array<BaseEntity>;
export type Connectors = Array<Connector>;
export type ConnectorGroups = Array<BaseEntity>;
export type Connections = Array<Connection>;
