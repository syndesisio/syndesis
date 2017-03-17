/* tslint:disable */

export interface BaseEntity {
    readonly id ? : string;
    // TODO we'll make this optional for now
    kind ? : string;
}

export interface ListResult extends BaseEntity {
    totalCount: number;
    items: Array < {} >
    ;
};
export type ListResults = Array < ListResult > ;

export interface ListResultWithId extends BaseEntity {
    totalCount: number;
    items: Array < WithId >
    ;
};
export type ListResultWithIds = Array < ListResultWithId > ;

export interface WithId extends BaseEntity {
    id: string;
};
export type WithIds = Array < WithId > ;

export interface Action extends BaseEntity {
    description: string;
    outputDataShape: DataShape;
    camelConnectorGAV: string;
    camelConnectorPrefix: string;
    connectorId: string;
    inputDataShape: DataShape;
    properties: {};
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    displayName: string;
    group: string;
    kind: string;
    description: string;
    required: boolean;
    secret: boolean;
    label: string;
    deprecated: boolean;
    type: string;
    defaultValue: string;
};
export type ComponentPropertys = Array < ComponentProperty > ;

export interface Connection extends BaseEntity {
    position: string;
    tags: Array < Tag >
    ;
    icon: string;
    organization: Organization;
    description: string;
    configuredProperties: {};
    userId: string;
    connector: Connector;
    organizationId: string;
    connectorId: string;
    id: string;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    icon: string;
    description: string;
    connectorGroup: ConnectorGroup;
    connectorGroupId: string;
    properties: {};
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

export interface DataShape extends BaseEntity {
    exemplar: Array < string >
    ;
    kind: string;
    schemaReference: string;
};
export type DataShapes = Array < DataShape > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    tags: Array < Tag >
    ;
    gitRepo: string;
    description: string;
    configuration: string;
    integrationTemplateId: string;
    integrationTemplate: IntegrationTemplate;
    userId: string;
    steps: Array < Step >
    ;
    statusType: "Activated" | "Deactivated";
    statusPhase: "Pending" | "Running" | "Succeeded" | "Failed" | "Unknown";
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
    userId: string;
    organizationId: string;
    id: string;
    name: string;
};
export type IntegrationTemplates = Array < IntegrationTemplate > ;

export interface Organization extends BaseEntity {
    environments: Array < Environment >
    ;
    users: Array < User >
    ;
    id: string;
    name: string;
};
export type Organizations = Array < Organization > ;

export interface Step extends BaseEntity {
    connection: Connection;
    action: Action;
    stepKind: string;
    configuredProperties: {};
    name: string;
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
    lastName: string;
    username: string;
    firstName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
    organizationId: string;
    name: string;
    id: string;
};
export type Users = Array < User > ;

export interface ListResultAction extends BaseEntity {
    totalCount: number;
    items: Array < Action >
    ;
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
            totalCount: undefined,
            items: undefined,
        };
    };

    createListResultWithId() {
        return <ListResultWithId > {
            totalCount: undefined,
            items: undefined,
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
            outputDataShape: undefined,
            camelConnectorGAV: undefined,
            camelConnectorPrefix: undefined,
            connectorId: undefined,
            inputDataShape: undefined,
            properties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createComponentProperty() {
        return <ComponentProperty > {
            javaType: undefined,
            displayName: undefined,
            group: undefined,
            kind: undefined,
            description: undefined,
            required: undefined,
            secret: undefined,
            label: undefined,
            deprecated: undefined,
            type: undefined,
            defaultValue: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            position: undefined,
            tags: undefined,
            icon: undefined,
            organization: undefined,
            description: undefined,
            configuredProperties: undefined,
            userId: undefined,
            connector: undefined,
            organizationId: undefined,
            connectorId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            icon: undefined,
            description: undefined,
            connectorGroup: undefined,
            connectorGroupId: undefined,
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

    createDataShape() {
        return <DataShape > {
            exemplar: undefined,
            kind: undefined,
            schemaReference: undefined,
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
            gitRepo: undefined,
            description: undefined,
            configuration: undefined,
            integrationTemplateId: undefined,
            integrationTemplate: undefined,
            userId: undefined,
            steps: undefined,
            statusType: undefined,
            statusPhase: undefined,
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
            userId: undefined,
            organizationId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createOrganization() {
        return <Organization > {
            environments: undefined,
            users: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createStep() {
        return <Step > {
            connection: undefined,
            action: undefined,
            stepKind: undefined,
            configuredProperties: undefined,
            name: undefined,
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
            lastName: undefined,
            username: undefined,
            firstName: undefined,
            integrations: undefined,
            roleId: undefined,
            organizationId: undefined,
            name: undefined,
            id: undefined,
        };
    };

    createListResultAction() {
        return <ListResultAction > {
            totalCount: undefined,
            items: undefined,
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