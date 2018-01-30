import { BaseEntity, User, Action } from '@syndesis/ui/platform';

export interface Connector extends BaseEntity {
  icon: string;
  properties: {};
  actions: Array<Action>;
  connectorGroupId: string;
  configuredProperties: {};
  description: string;
  connectorGroup: BaseEntity;
  tags: Array<string>;
}

export interface Organization extends BaseEntity {
  environments: Array<BaseEntity>;
  users: Array<User>;
}

export type Organizations = Array<Organization>;

export interface Connection extends BaseEntity {
  icon: string;
  organization: Organization;
  configuredProperties: {};
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
}

export type Environments = Array<BaseEntity>;
export type Connectors = Array<Connector>;
export type ConnectorGroups = Array<BaseEntity>;
export type Connections = Array<Connection>;
