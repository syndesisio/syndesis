export interface ListResult {
    items: Array < {} >
    ;
    totalCount: number;
};

export interface ListResultWithId {
    items: Array < WithId >
    ;
    totalCount: number;
};

export interface WithId {
    id: string;
};

export interface Connection {
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

export interface Connector {
    icon: string;
    connectorGroup: ConnectorGroup;
    connectorGroupId: string;
    description: string;
    properties: string;
    id: string;
    name: string;
};

export interface ConnectorGroup {
    id: string;
    name: string;
};

export interface Environment {
    id: string;
    name: string;
};

export interface Integration {
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

export interface IntegrationPattern {
    icon: string;
    integrationPatternGroupId: string;
    integrationPatternGroup: IntegrationPatternGroup;
    properties: string;
    id: string;
    name: string;
};

export interface IntegrationPatternGroup {
    id: string;
    name: string;
};

export interface IntegrationTemplate {
    organization: Organization;
    configuration: string;
    userId: string;
    organizationId: string;
    id: string;
    name: string;
};

export interface Organization {
    users: Array < User >
    ;
    environments: Array < Environment >
    ;
    id: string;
    name: string;
};

export interface Step {
    configuredProperties: string;
    integrationPattern: IntegrationPattern;
    id: string;
};

export interface Tag {
    integrationTemplate: Array < IntegrationTemplate >
    ;
    connections: Array < Connection >
    ;
    id: string;
    name: string;
};

export interface User {
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