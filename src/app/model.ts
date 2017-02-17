/* tslint:disable */

export interface BaseEntity {
    readonly id ? : string;
    // TODO we'll make this optional for now
    kind ? : string;
}

export interface ListResult extends BaseEntity {
    items: Array < {} >
    ;
    totalCount: number;
};
export type ListResults = Array < ListResult > ;

export interface ListResultWithId extends BaseEntity {
    items: Array < WithId >
    ;
    totalCount: number;
};
export type ListResultWithIds = Array < ListResultWithId > ;

export interface WithId extends BaseEntity {
    id: string;
};
export type WithIds = Array < WithId > ;

export interface Connection extends BaseEntity {
    position: string;
    organization: Organization;
    icon: string;
    tags: Array < Tag >
    ;
    configuredProperties: string;
    description: string;
    userId: string;
    connector: Connector;
    connectorId: string;
    organizationId: string;
    id: string;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    icon: string;
    connectorGroup: ConnectorGroup;
    connectorGroupId: string;
    description: string;
    properties: string;
    id: string;
    name: string;
};
export type Connectors = Array < Connector > ;

export interface ConnectorGroup extends BaseEntity {
    id: string;
    name: string;
};
export type ConnectorGroups = Array < ConnectorGroup > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    tags: Array < Tag >
    ;
    description: string;
    users: Array < User >
    ;
    configuration: string;
    integrationTemplate: IntegrationTemplate;
    userId: string;
    steps: Array < Step >
    ;
    integrationTemplateId: string;
    connections: Array < Connection >
    ;
    id: string;
    name: string;
};
export type Integrations = Array < Integration > ;

export interface IntegrationPattern extends BaseEntity {
    icon: string;
    integrationPatternGroupId: string;
    integrationPatternGroup: IntegrationPatternGroup;
    properties: string;
    id: string;
    name: string;
};
export type IntegrationPatterns = Array < IntegrationPattern > ;

export interface IntegrationPatternGroup extends BaseEntity {
    id: string;
    name: string;
};
export type IntegrationPatternGroups = Array < IntegrationPatternGroup > ;

export interface IntegrationTemplate extends BaseEntity {
    organization: Organization;
    configuration: string;
    userId: string;
    organizationId: string;
    id: string;
    name: string;
};
export type IntegrationTemplates = Array < IntegrationTemplate > ;

export interface Organization extends BaseEntity {
    users: Array < User >
    ;
    environments: Array < Environment >
    ;
    id: string;
    name: string;
};
export type Organizations = Array < Organization > ;

export interface Step extends BaseEntity {
    configuredProperties: string;
    integrationPattern: IntegrationPattern;
    id: string;
};
export type Steps = Array < Step > ;

export interface Tag extends BaseEntity {
    integrationTemplate: Array < IntegrationTemplate >
    ;
    connections: Array < Connection >
    ;
    id: string;
    name: string;
};
export type Tags = Array < Tag > ;

export interface User extends BaseEntity {
    fullName: string;
    username: string;
    lastName: string;
    firstName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
    organizationId: string;
    name: string;
    id: string;
};
export type Users = Array < User > ;