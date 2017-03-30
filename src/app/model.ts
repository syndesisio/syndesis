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
    properties: {};
    description: string;
    camelConnectorPrefix: string;
    connectorId: string;
    camelConnectorGAV: string;
    outputDataShape: DataShape;
    inputDataShape: DataShape;
    id: string;
    name: string;
};
export type Actions = Array < Action > ;

export interface ComponentProperty extends BaseEntity {
    javaType: string;
    type: string;
    defaultValue: string;
    displayName: string;
    required: boolean;
    secret: boolean;
    label: string;
    description: string;
    kind: string;
    deprecated: boolean;
    group: string;
};
export type ComponentPropertys = Array < ComponentProperty > ;

export interface Connection extends BaseEntity {
    position: string;
    organization: Organization;
    description: string;
    options: {};
    connector: Connector;
    tags: Array < Tag >
    ;
    icon: string;
    connectorId: string;
    configuredProperties: {};
    organizationId: string;
    userId: string;
    id: string;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    properties: {};
    actions: Array < Action >
    ;
    description: string;
    connectorGroupId: string;
    icon: string;
    connectorGroup: ConnectorGroup;
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
    connections: Array < Connection >
    ;
    timesUsed: number;
    description: string;
    users: Array < User >
    ;
    configuration: string;
    gitRepo: string;
    tags: Array < Tag >
    ;
    currentStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
    steps: Array < Step >
    ;
    desiredStatus: "Draft" | "Activated" | "Deactivated" | "Deleted";
    integrationTemplateId: string;
    integrationTemplate: IntegrationTemplate;
    lastUpdated: string;
    userId: string;
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
    name: string;
    connection: Connection;
    stepKind: string;
    configuredProperties: {};
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
    name: string;
    fullName: string;
    firstName: string;
    username: string;
    roleId: string;
    integrations: Array < Integration >
    ;
    lastName: string;
    organizationId: string;
    id: string;
};
export type Users = Array < User > ;

export interface ListResultAction extends BaseEntity {
    items: Array < Action >
    ;
    totalCount: number;
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
    errors: Array < Error >
    ;
    scope: "PARAMETERS" | "CONNECTIVITY";
    status: "OK" | "ERROR" | "UNSUPPORTED";
};
export type Results = Array < Result > ;

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
            properties: undefined,
            description: undefined,
            camelConnectorPrefix: undefined,
            connectorId: undefined,
            camelConnectorGAV: undefined,
            outputDataShape: undefined,
            inputDataShape: undefined,
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
            required: undefined,
            secret: undefined,
            label: undefined,
            description: undefined,
            kind: undefined,
            deprecated: undefined,
            group: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            position: undefined,
            organization: undefined,
            description: undefined,
            options: undefined,
            connector: undefined,
            tags: undefined,
            icon: undefined,
            connectorId: undefined,
            configuredProperties: undefined,
            organizationId: undefined,
            userId: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            properties: undefined,
            actions: undefined,
            description: undefined,
            connectorGroupId: undefined,
            icon: undefined,
            connectorGroup: undefined,
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
            connections: undefined,
            timesUsed: undefined,
            description: undefined,
            users: undefined,
            configuration: undefined,
            gitRepo: undefined,
            tags: undefined,
            currentStatus: undefined,
            steps: undefined,
            desiredStatus: undefined,
            integrationTemplateId: undefined,
            integrationTemplate: undefined,
            lastUpdated: undefined,
            userId: undefined,
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
            name: undefined,
            connection: undefined,
            stepKind: undefined,
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
            name: undefined,
            fullName: undefined,
            firstName: undefined,
            username: undefined,
            roleId: undefined,
            integrations: undefined,
            lastName: undefined,
            organizationId: undefined,
            id: undefined,
        };
    };

    createListResultAction() {
        return <ListResultAction > {
            items: undefined,
            totalCount: undefined,
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
            errors: undefined,
            scope: undefined,
            status: undefined,
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