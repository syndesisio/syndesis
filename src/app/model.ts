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
    properties: {};
    camelConnectorGAV: string;
    camelConnectorPrefix: string;
    description: string;
    inputDataShape: DataShape;
    outputDataShape: DataShape;
    connectorId: string;
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    type: string;
    defaultValue: string;
    displayName: string;
    kind: string;
    secret: boolean;
    label: string;
    description: string;
    group: string;
    required: boolean;
    deprecated: boolean;
};
export type ComponentPropertys = Array < ComponentProperty > ;

export interface Connection extends BaseEntity {
    icon: string;
    organization: Organization;
    options: {};
    description: string;
    connector: Connector;
    userId: string;
    lastUpdated: string;
    createdDate: string;
    connectorId: string;
    organizationId: string;
    configuredProperties: {};
    id: string;
    tags: Array < string >
    ;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    icon: string;
    properties: {};
    actions: Array < Action >
    ;
    connectorGroup: ConnectorGroup;
    description: string;
    connectorGroupId: string;
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
    type: string;
    exemplar: Array < string >
    ;
    kind: string;
};
export type DataShapes = Array < DataShape > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    statusMessage: string;
    description: string;
    gitRepo: string;
    desiredStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    currentStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    configuration: string;
    token: string;
    connections: Array < Connection >
    ;
    users: Array < User >
    ;
    integrationTemplateId: string;
    integrationTemplate: IntegrationTemplate;
    userId: string;
    steps: Array < Step >
    ;
    lastUpdated: string;
    createdDate: string;
    timesUsed: number;
    id: string;
    tags: Array < string >
    ;
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
    connection: Connection;
    name: string;
    stepKind: string;
    configuredProperties: {};
    id: string;
};
export type Steps = Array < Step > ;

export interface User extends BaseEntity {
    name: string;
    username: string;
    fullName: string;
    lastName: string;
    firstName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
    organizationId: string;
    id: string;
};
export type Users = Array < User > ;

export interface ListResultAction extends BaseEntity {
    totalCount: number;
    items: Array < Action >
    ;
};
export type ListResultActions = Array < ListResultAction > ;

export interface Error extends BaseEntity {
    parameters: Array < string >
    ;
    attributes: {};
    description: string;
    code: string;
};
export type Errors = Array < Error > ;

export interface Result extends BaseEntity {
    scope: "PARAMETERS" | "CONNECTIVITY";
    errors: Array < Error >
    ;
    status: "OK" | "ERROR" | "UNSUPPORTED";
};
export type Results = Array < Result > ;

export interface EventMessage extends BaseEntity {
    data: {};
    event: string;
};
export type EventMessages = Array < EventMessage > ;

export interface ListResultString extends BaseEntity {
    totalCount: number;
    items: Array < string >
    ;
};
export type ListResultStrings = Array < ListResultString > ;

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
            properties: undefined,
            camelConnectorGAV: undefined,
            camelConnectorPrefix: undefined,
            description: undefined,
            inputDataShape: undefined,
            outputDataShape: undefined,
            connectorId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createComponentProperty() {
        return <ComponentProperty > {
            javaType: undefined,
            type: undefined,
            defaultValue: undefined,
            displayName: undefined,
            kind: undefined,
            secret: undefined,
            label: undefined,
            description: undefined,
            group: undefined,
            required: undefined,
            deprecated: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            icon: undefined,
            organization: undefined,
            options: undefined,
            description: undefined,
            connector: undefined,
            userId: undefined,
            lastUpdated: undefined,
            createdDate: undefined,
            connectorId: undefined,
            organizationId: undefined,
            configuredProperties: undefined,
            id: undefined,
            tags: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            icon: undefined,
            properties: undefined,
            actions: undefined,
            connectorGroup: undefined,
            description: undefined,
            connectorGroupId: undefined,
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
            type: undefined,
            exemplar: undefined,
            kind: undefined,
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
            statusMessage: undefined,
            description: undefined,
            gitRepo: undefined,
            desiredStatus: undefined,
            currentStatus: undefined,
            configuration: undefined,
            token: undefined,
            connections: undefined,
            users: undefined,
            integrationTemplateId: undefined,
            integrationTemplate: undefined,
            userId: undefined,
            steps: undefined,
            lastUpdated: undefined,
            createdDate: undefined,
            timesUsed: undefined,
            id: undefined,
            tags: undefined,
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
            connection: undefined,
            name: undefined,
            stepKind: undefined,
            configuredProperties: undefined,
            id: undefined,
        };
    };

    createUser() {
        return <User > {
            name: undefined,
            username: undefined,
            fullName: undefined,
            lastName: undefined,
            firstName: undefined,
            integrations: undefined,
            roleId: undefined,
            organizationId: undefined,
            id: undefined,
        };
    };

    createListResultAction() {
        return <ListResultAction > {
            totalCount: undefined,
            items: undefined,
        };
    };

    createError() {
        return <Error > {
            parameters: undefined,
            attributes: undefined,
            description: undefined,
            code: undefined,
        };
    };

    createResult() {
        return <Result > {
            scope: undefined,
            errors: undefined,
            status: undefined,
        };
    };

    createEventMessage() {
        return <EventMessage > {
            data: undefined,
            event: undefined,
        };
    };

    createListResultString() {
        return <ListResultString > {
            totalCount: undefined,
            items: undefined,
        };
    };

};

export const TypeFactory = new TypeFactoryClass();
