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
    camelConnectorGAV: string;
    outputDataShape: DataShape;
    inputDataShape: DataShape;
    camelConnectorPrefix: string;
    properties: {};
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    displayName: string;
    secret: boolean;
    label: string;
    group: string;
    description: string;
    required: boolean;
    kind: string;
    deprecated: boolean;
    type: string;
    defaultValue: string;
};
export type ComponentPropertys = Array < ComponentProperty > ;

export interface Connection extends BaseEntity {
    organization: Organization;
    position: string;
    connectorId: string;
    connector: Connector;
    description: string;
    tags: Array < Tag >
    ;
    icon: string;
    organizationId: string;
    userId: string;
    configuredProperties: {};
    id: string;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    description: string;
    icon: string;
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
    dataType: string;
    exemplar: Array < string >
    ;
};
export type DataShapes = Array < DataShape > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    description: string;
    tags: Array < Tag >
    ;
    configuration: string;
    integrationTemplateId: string;
    users: Array < User >
    ;
    gitRepo: string;
    connections: Array < Connection >
    ;
    integrationTemplate: IntegrationTemplate;
    userId: string;
    steps: Array < Step >
    ;
    desiredStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
    currentStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
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
    action: Action;
    connection: Connection;
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
    organizationId: string;
    lastName: string;
    firstName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
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
            camelConnectorGAV: undefined,
            outputDataShape: undefined,
            inputDataShape: undefined,
            camelConnectorPrefix: undefined,
            properties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createComponentProperty() {
        return <ComponentProperty > {
            javaType: undefined,
            displayName: undefined,
            secret: undefined,
            label: undefined,
            group: undefined,
            description: undefined,
            required: undefined,
            kind: undefined,
            deprecated: undefined,
            type: undefined,
            defaultValue: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            organization: undefined,
            position: undefined,
            connectorId: undefined,
            connector: undefined,
            description: undefined,
            tags: undefined,
            icon: undefined,
            organizationId: undefined,
            userId: undefined,
            configuredProperties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            description: undefined,
            icon: undefined,
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
            dataType: undefined,
            exemplar: undefined,
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
            description: undefined,
            tags: undefined,
            configuration: undefined,
            integrationTemplateId: undefined,
            users: undefined,
            gitRepo: undefined,
            connections: undefined,
            integrationTemplate: undefined,
            userId: undefined,
            steps: undefined,
            desiredStatus: undefined,
            currentStatus: undefined,
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
            action: undefined,
            connection: undefined,
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
            organizationId: undefined,
            lastName: undefined,
            firstName: undefined,
            integrations: undefined,
            roleId: undefined,
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