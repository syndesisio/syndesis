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
    connectorId: string;
    description: string;
    camelConnectorPrefix: string;
    inputDataShape: DataShape;
    outputDataShape: DataShape;
    camelConnectorGAV: string;
    properties: {};
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    displayName: string;
    group: string;
    description: string;
    secret: boolean;
    label: string;
    required: boolean;
    kind: string;
    deprecated: boolean;
    type: string;
    defaultValue: string;
};
export type ComponentPropertys = Array < ComponentProperty > ;

export interface Connection extends BaseEntity {
    tags: Array < Tag >
    ;
    icon: string;
    organization: Organization;
    connectorId: string;
    connector: Connector;
    userId: string;
    description: string;
    position: string;
    configuredProperties: {};
    organizationId: string;
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
    userId: string;
    steps: Array < Step >
    ;
    statusType: "Activated" | "Deactivated" | "Draft";
    connections: Array < Connection >
    ;
    description: string;
    configuration: string;
    gitRepo: string;
    integrationTemplate: IntegrationTemplate;
    users: Array < User >
    ;
    statusPhase: "Pending" | "Running" | "Succeeded" | "Failed" | "Unknown";
    integrationTemplateId: string;
    id: string;
    name: string;
};
export type Integrations = Array < Integration > ;

export interface IntegrationTemplate extends BaseEntity {
    organization: Organization;
    userId: string;
    configuration: string;
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
    connection: Connection;
    action: Action;
    stepKind: string;
    configuredProperties: {};
    name: string;
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
    fullName: string;
    roleId: string;
    lastName: string;
    integrations: Array < Integration >
    ;
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
            connectorId: undefined,
            description: undefined,
            camelConnectorPrefix: undefined,
            inputDataShape: undefined,
            outputDataShape: undefined,
            camelConnectorGAV: undefined,
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
            description: undefined,
            secret: undefined,
            label: undefined,
            required: undefined,
            kind: undefined,
            deprecated: undefined,
            type: undefined,
            defaultValue: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            tags: undefined,
            icon: undefined,
            organization: undefined,
            connectorId: undefined,
            connector: undefined,
            userId: undefined,
            description: undefined,
            position: undefined,
            configuredProperties: undefined,
            organizationId: undefined,
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
            userId: undefined,
            steps: undefined,
            statusType: undefined,
            connections: undefined,
            description: undefined,
            configuration: undefined,
            gitRepo: undefined,
            integrationTemplate: undefined,
            users: undefined,
            statusPhase: undefined,
            integrationTemplateId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createIntegrationTemplate() {
        return <IntegrationTemplate > {
            organization: undefined,
            userId: undefined,
            configuration: undefined,
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
            connections: undefined,
            integrationTemplate: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createUser() {
        return <User > {
            username: undefined,
            fullName: undefined,
            roleId: undefined,
            lastName: undefined,
            integrations: undefined,
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

};

export const TypeFactory = new TypeFactoryClass();