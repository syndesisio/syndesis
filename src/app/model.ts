/* tslint:disable */

export interface BaseEntity {
    readonly id ? : string;
    // TODO we'll make this optional for now
    kind ? : string;
}

// TODO local hack to avoid deleting all the related code
export interface IntegrationTemplate extends BaseEntity {

}
export type IntegrationTemplates = Array<IntegrationTemplate>;

export interface Action extends BaseEntity {
    definition: ActionDefinition;
    camelConnectorGAV: string;
    camelConnectorPrefix: string;
    description: string;
    connectorId: string;
    id: string;
    name: string;
    tags: Array < string >
    ;
    // TODO DEPRECATED
    outputDataShape: DataShape;
    inputDataShape: DataShape;
};
export type Actions = Array < Action > ;

export interface ActionDefinition extends BaseEntity {
    propertyDefinitionSteps: Array < ActionDefinitionStep >
    ;
    outputDataShape: DataShape;
    inputDataShape: DataShape;
};
export type ActionDefinitions = Array < ActionDefinition > ;

export interface ActionDefinitionStep extends BaseEntity {
    description: string;
    name: string;
    properties: {};
    configuredProperties: {};
};
export type ActionDefinitionSteps = Array < ActionDefinitionStep > ;

export interface ConfigurationProperty extends BaseEntity {
    javaType: string;
    type: string;
    defaultValue: string;
    displayName: string;
    required: boolean;
    enum: Array < PropertyValue >
    ;
    description: string;
    deprecated: boolean;
    componentProperty: boolean;
    kind: string;
    secret: boolean;
    label: string;
    group: string;
    tags: Array < string >
    ;
};
export type ConfigurationProperties = Array < ConfigurationProperty > ;

export interface Connection extends BaseEntity {
    icon: string;
    organization: Organization;
    connector: Connector;
    configuredProperties: {};
    createdDate: string;
    description: string;
    userId: string;
    connectorId: string;
    organizationId: string;
    options: {};
    derived: boolean;
    lastUpdated: string;
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
    configuredProperties: {};
    description: string;
    connectorGroup: ConnectorGroup;
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
    specification: string;
    exemplar: Array < string >
    ;
    type: string;
    kind: string;
};
export type DataShapes = Array < DataShape > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    configuration: string;
    steps: Array < Step >
    ;
    desiredStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    currentStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    statusMessage: string;
    createdDate: string;
    description: string;
    userId: string;
    timesUsed: number;
    users: Array < User >
    ;
    stepsDone: Array < string >
    ;
    gitRepo: string;
    token: string;
    connections: Array < Connection >
    ;
    lastUpdated: string;
    integrationTemplateId: string;
    id: string;
    tags: Array < string >
    ;
    name: string;
};
export type Integrations = Array < Integration > ;

export interface Organization extends BaseEntity {
    environments: Array < Environment >
    ;
    users: Array < User >
    ;
    id: string;
    name: string;
};
export type Organizations = Array < Organization > ;

export interface PropertyValue extends BaseEntity {
    value: string;
    label: string;
};
export type PropertyValues = Array < PropertyValue > ;

export interface Step extends BaseEntity {
    connection: Connection;
    action: Action;
    name: string;
    stepKind: string;
    configuredProperties: {};
    id: string;
};
export type Steps = Array < Step > ;

export interface User extends BaseEntity {
    fullName: string;
    name: string;
    username: string;
    firstName: string;
    integrations: Array < Integration >
    ;
    lastName: string;
    organizationId: string;
    roleId: string;
    id: string;
};
export type Users = Array < User > ;

export interface ListResult extends BaseEntity {
    totalCount: number;
    items: Array < {} >
    ;
};
export type ListResults = Array < ListResult > ;

export interface ListResultWithIdObject extends BaseEntity {
    totalCount: number;
    items: Array < WithIdObject >
    ;
};
export type ListResultWithIdObjects = Array < ListResultWithIdObject > ;

export interface WithId extends BaseEntity {
    id: string;
};
export type WithIds = Array < WithId > ;

export interface WithIdObject extends BaseEntity {
    id: string;
};
export type WithIdObjects = Array < WithIdObject > ;

export interface Violation extends BaseEntity {};
export type Violations = Array < Violation > ;

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

export interface FilterOptions extends BaseEntity {
    paths: Array < string >
    ;
    ops: Array < Op >
    ;
};
export type FilterOptionss = Array < FilterOptions > ;

export interface Op extends BaseEntity {
    operator: string;
    label: string;
};
export type Ops = Array < Op > ;

export interface AcquisitionMethod extends BaseEntity {
    icon: string;
    type: "OAUTH1" | "OAUTH2";
    description: string;
    label: string;
};
export type AcquisitionMethods = Array < AcquisitionMethod > ;

export interface AcquisitionRequest extends BaseEntity {
    returnUrl: string;
};
export type AcquisitionRequests = Array < AcquisitionRequest > ;

export interface EventMessage extends BaseEntity {
    data: {};
    event: string;
};
export type EventMessages = Array < EventMessage > ;

export interface OAuthApp extends BaseEntity {
    id: string;
    name: string;
    icon: string;
    clientId: string;
    clientSecret: string;
};
export type OAuthApps = Array < OAuthApp > ;

export interface ListResultString extends BaseEntity {
    totalCount: number;
    items: Array < string >
    ;
};
export type ListResultStrings = Array < ListResultString > ;

class TypeFactoryClass {
    createAction() {
        return <Action > {
            definition: undefined,
            camelConnectorGAV: undefined,
            camelConnectorPrefix: undefined,
            description: undefined,
            connectorId: undefined,
            id: undefined,
            name: undefined,
            tags: undefined,
        };
    };

    createActionDefinition() {
        return <ActionDefinition > {
            propertyDefinitionSteps: undefined,
            outputDataShape: undefined,
            inputDataShape: undefined,
        };
    };

    createActionDefinitionStep() {
        return <ActionDefinitionStep > {
            description: undefined,
            name: undefined,
            properties: undefined,
            configuredProperties: undefined,
        };
    };

    createConfigurationProperty() {
        return <ConfigurationProperty > {
            javaType: undefined,
            type: undefined,
            defaultValue: undefined,
            displayName: undefined,
            required: undefined,
            enum: undefined,
            description: undefined,
            deprecated: undefined,
            componentProperty: undefined,
            kind: undefined,
            secret: undefined,
            label: undefined,
            group: undefined,
            tags: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            icon: undefined,
            organization: undefined,
            connector: undefined,
            configuredProperties: undefined,
            createdDate: undefined,
            description: undefined,
            userId: undefined,
            connectorId: undefined,
            organizationId: undefined,
            options: undefined,
            derived: undefined,
            lastUpdated: undefined,
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
            configuredProperties: undefined,
            description: undefined,
            connectorGroup: undefined,
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
            specification: undefined,
            exemplar: undefined,
            type: undefined,
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
            configuration: undefined,
            steps: undefined,
            desiredStatus: undefined,
            currentStatus: undefined,
            statusMessage: undefined,
            createdDate: undefined,
            description: undefined,
            userId: undefined,
            timesUsed: undefined,
            users: undefined,
            stepsDone: undefined,
            gitRepo: undefined,
            token: undefined,
            connections: undefined,
            lastUpdated: undefined,
            integrationTemplateId: undefined,
            id: undefined,
            tags: undefined,
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

    createPropertyValue() {
        return <PropertyValue > {
            value: undefined,
            label: undefined,
        };
    };

    createStep() {
        return <Step > {
            connection: undefined,
            action: undefined,
            name: undefined,
            stepKind: undefined,
            configuredProperties: undefined,
            id: undefined,
        };
    };

    createUser() {
        return <User > {
            fullName: undefined,
            name: undefined,
            username: undefined,
            firstName: undefined,
            integrations: undefined,
            lastName: undefined,
            organizationId: undefined,
            roleId: undefined,
            id: undefined,
        };
    };

    createListResult() {
        return <ListResult > {
            totalCount: undefined,
            items: undefined,
        };
    };

    createListResultWithIdObject() {
        return <ListResultWithIdObject > {
            totalCount: undefined,
            items: undefined,
        };
    };

    createWithId() {
        return <WithId > {
            id: undefined,
        };
    };

    createWithIdObject() {
        return <WithIdObject > {
            id: undefined,
        };
    };

    createViolation() {
        return <Violation > {};
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

    createFilterOptions() {
        return <FilterOptions > {
            paths: undefined,
            ops: undefined,
        };
    };

    createOp() {
        return <Op > {
            operator: undefined,
            label: undefined,
        };
    };

    createAcquisitionMethod() {
        return <AcquisitionMethod > {
            icon: undefined,
            type: undefined,
            description: undefined,
            label: undefined,
        };
    };

    createAcquisitionRequest() {
        return <AcquisitionRequest > {
            returnUrl: undefined,
        };
    };

    createEventMessage() {
        return <EventMessage > {
            data: undefined,
            event: undefined,
        };
    };

    createOAuthApp() {
        return <OAuthApp > {
            id: undefined,
            name: undefined,
            icon: undefined,
            clientId: undefined,
            clientSecret: undefined,
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
