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
    properties: string;
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface Connection extends BaseEntity {
    organization: Organization;
    tags: Array < Tag >
    ;
    icon: string;
    position: string;
    description: string;
    connector: Connector;
    configuredProperties: string;
    userId: string;
    connectorId: string;
    organizationId: string;
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
    connections: Array < Connection >
    ;
    description: string;
    configuration: string;
    integrationTemplate: IntegrationTemplate;
    users: Array < User >
    ;
    userId: string;
    integrationTemplateId: string;
    steps: Array < Step >
    ;
    id: string;
    name: string;
};
export type Integrations = Array < Integration > ;

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
    action: Action;
    configuredProperties: string;
    id: string;
};
export type Steps = Array < Step > ;

export interface Tag extends BaseEntity {
    connections: Array < Connection >
    ;
    integrationTemplate: Array < IntegrationTemplate >
    ;
    id: string;
    name: string;
};
export type Tags = Array < Tag > ;

export interface User extends BaseEntity {
    username: string;
    firstName: string;
    fullName: string;
    lastName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
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
            properties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            organization: undefined,
            tags: undefined,
            icon: undefined,
            position: undefined,
            description: undefined,
            connector: undefined,
            configuredProperties: undefined,
            userId: undefined,
            connectorId: undefined,
            organizationId: undefined,
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
            connections: undefined,
            description: undefined,
            configuration: undefined,
            integrationTemplate: undefined,
            users: undefined,
            userId: undefined,
            integrationTemplateId: undefined,
            steps: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createIntegrationTemplate() {
        return <IntegrationTemplate > {
            organization: undefined,
            configuration: undefined,
            userId: undefined,
            organizationId: undefined,
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
            action: undefined,
            configuredProperties: undefined,
            id: undefined,
        };
    };

    createTag() {
        return <Tag > {
            connections: undefined,
            integrationTemplate: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createUser() {
        return <User > {
            username: undefined,
            firstName: undefined,
            fullName: undefined,
            lastName: undefined,
            integrations: undefined,
            roleId: undefined,
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