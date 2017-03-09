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

export interface Action extends BaseEntity {
    description: string;
    connectorId: string;
    camelConnectorGAV: string;
    camelConnectorPrefix: string;
    properties: string;
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface Connection extends BaseEntity {
    position: string;
    organization: Organization;
    tags: Array < Tag >
    ;
    icon: string;
    description: string;
    connector: Connector;
    configuredProperties: string;
    organizationId: string;
    connectorId: string;
    userId: string;
    id: string;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    icon: string;
    description: string;
    connectorGroupId: string;
    connectorGroup: ConnectorGroup;
    properties: string;
    actions: Array < Action >
    ;
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
    configuration: string;
    gitRepo: string;
    integrationTemplateId: string;
    integrationTemplate: IntegrationTemplate;
    userId: string;
    steps: Array < Step >
    ;
    users: Array < User >
    ;
    connections: Array < Connection >
    ;
    id: string;
    name: string;
};
export type Integrations = Array < Integration > ;

export interface IntegrationTemplate extends BaseEntity {
    organization: Organization;
    configuration: string;
    organizationId: string;
    userId: string;
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
    connection: Connection;
    action: Action;
    configuredProperties: string;
    stepKind: string;
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
    integrations: Array < Integration >
    ;
    roleId: string;
    firstName: string;
    organizationId: string;
    name: string;
    id: string;
};
export type Users = Array < User > ;

export interface ListResultAction extends BaseEntity {
    items: Array < Action >
    ;
    totalCount: number;
};
export type ListResultActions = Array < ListResultAction > ;

export interface EventMessage extends BaseEntity {
    data: {};
    event: string;
};
export type EventMessages = Array < EventMessage > ;

export interface InputStream extends BaseEntity {};
export type InputStreams = Array < InputStream > ;

class TypeFactoryClass {
    createListResult() {
        return <ListResult > {
            items: undefined,
            totalCount: undefined,
        };
    };

    createListResultWithId() {
        return <ListResultWithId > {
            items: undefined,
            totalCount: undefined,
        };
    };

    createWithId() {
        return <WithId > {
            id: undefined,
        };
    };

    createAction() {
        return <Action > {
            description: undefined,
            connectorId: undefined,
            camelConnectorGAV: undefined,
            camelConnectorPrefix: undefined,
            properties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            position: undefined,
            organization: undefined,
            tags: undefined,
            icon: undefined,
            description: undefined,
            connector: undefined,
            configuredProperties: undefined,
            organizationId: undefined,
            connectorId: undefined,
            userId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            icon: undefined,
            description: undefined,
            connectorGroupId: undefined,
            connectorGroup: undefined,
            properties: undefined,
            actions: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnectorGroup() {
        return <ConnectorGroup > {
            id: undefined,
            name: undefined,
        };
    };

    createEnvironment() {
        return <Environment > {
            id: undefined,
            name: undefined,
        };
    };

    createIntegration() {
        return <Integration > {
            tags: undefined,
            description: undefined,
            configuration: undefined,
            gitRepo: undefined,
            integrationTemplateId: undefined,
            integrationTemplate: undefined,
            userId: undefined,
            steps: undefined,
            users: undefined,
            connections: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createIntegrationTemplate() {
        return <IntegrationTemplate > {
            organization: undefined,
            configuration: undefined,
            organizationId: undefined,
            userId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createOrganization() {
        return <Organization > {
            users: undefined,
            environments: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createStep() {
        return <Step > {
            connection: undefined,
            action: undefined,
            configuredProperties: undefined,
            stepKind: undefined,
            id: undefined,
        };
    };

    createTag() {
        return <Tag > {
            integrationTemplate: undefined,
            connections: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createUser() {
        return <User > {
            fullName: undefined,
            username: undefined,
            lastName: undefined,
            integrations: undefined,
            roleId: undefined,
            firstName: undefined,
            organizationId: undefined,
            name: undefined,
            id: undefined,
        };
    };

    createListResultAction() {
        return <ListResultAction > {
            items: undefined,
            totalCount: undefined,
        };
    };

    createEventMessage() {
        return <EventMessage > {
            data: undefined,
            event: undefined,
        };
    };

    createInputStream() {
        return <InputStream > {};
    };

};

export const TypeFactory = new TypeFactoryClass();