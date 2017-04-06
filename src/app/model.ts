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
    outputDataShape: DataShape;
    connectorId: string;
    inputDataShape: DataShape;
    camelConnectorGAV: string;
    camelConnectorPrefix: string;
    properties: {};
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    displayName: string;
    group: string;
    required: boolean;
    description: string;
    kind: string;
    secret: boolean;
    label: string;
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
    position: string;
    description: string;
    userId: string;
    options: {};
    connectorId: string;
    configuredProperties: {};
    connector: Connector;
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
    kind: string;
    exemplar: Array < string >
    ;
    type: string;
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
    integrationTemplateId: string;
    description: string;
    connections: Array < Connection >
    ;
    userId: string;
    users: Array < User >
    ;
    configuration: string;
    currentStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
    steps: Array < Step >
    ;
    gitRepo: string;
    integrationTemplate: IntegrationTemplate;
    desiredStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
    statusMessage: string;
    lastUpdated: string;
    timesUsed: number;
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
    lastName: string;
    username: string;
    firstName: string;
    fullName: string;
    roleId: string;
    integrations: Array < Integration >
    ;
    organizationId: string;
    name: string;
    id: string;
};
export type Users = Array < User > ;

export interface Error extends BaseEntity {
    description: string;
    code: string;
    parameters: Array < string >
    ;
    attributes: {};
};
export type Errors = Array < Error > ;

export interface Result extends BaseEntity {
    errors: Array < Error >
    ;
    scope: "PARAMETERS" | "CONNECTIVITY";
    status: "OK" | "ERROR" | "UNSUPPORTED";
};
export type Results = Array < Result > ;

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
            description: undefined,
            outputDataShape: undefined,
            connectorId: undefined,
            inputDataShape: undefined,
            camelConnectorGAV: undefined,
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
            group: undefined,
            required: undefined,
            description: undefined,
            kind: undefined,
            secret: undefined,
            label: undefined,
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
            position: undefined,
            description: undefined,
            userId: undefined,
            options: undefined,
            connectorId: undefined,
            configuredProperties: undefined,
            connector: undefined,
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
            kind: undefined,
            exemplar: undefined,
            type: undefined,
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
            integrationTemplateId: undefined,
            description: undefined,
            connections: undefined,
            userId: undefined,
            users: undefined,
            configuration: undefined,
            currentStatus: undefined,
            steps: undefined,
            gitRepo: undefined,
            integrationTemplate: undefined,
            desiredStatus: undefined,
            statusMessage: undefined,
            lastUpdated: undefined,
            timesUsed: undefined,
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
            lastName: undefined,
            username: undefined,
            firstName: undefined,
            fullName: undefined,
            roleId: undefined,
            integrations: undefined,
            organizationId: undefined,
            name: undefined,
            id: undefined,
        };
    };

    createError() {
        return <Error > {
            description: undefined,
            code: undefined,
            parameters: undefined,
            attributes: undefined,
        };
    };

    createResult() {
        return <Result > {
            errors: undefined,
            scope: undefined,
            status: undefined,
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