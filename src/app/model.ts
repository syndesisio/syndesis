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

export interface ListResultWithIdObject extends BaseEntity {
    items: Array < WithIdObject >
    ;
    totalCount: number;
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

export interface Acquisition extends BaseEntity {
    url: string;
    type: "REDIRECT";
};
export type Acquisitions = Array < Acquisition > ;

export interface AcquisitionRequest extends BaseEntity {};
export type AcquisitionRequests = Array < AcquisitionRequest > ;

export interface PropertyDefinitionStep extends BaseEntity {
  name: string;
  description: string;
  properties: {};
};

export type PropertyDefinitionSteps = Array < PropertyDefinitionStep > ;

export interface ActionDefinition extends BaseEntity {
  propertyDefinitionSteps: PropertyDefinitionSteps;
};

export interface Action extends BaseEntity {
    camelConnectorGAV: string;
    connectorId: string;
    description: string;
    camelConnectorPrefix: string;
    outputDataShape: DataShape;
    inputDataShape: DataShape;
    id: string;
    name: string;
    definition: ActionDefinition;
};
export type Actions = Array < Action > ;

export interface ConfigurationProperty extends BaseEntity {
    javaType: string;
    displayName: string;
    secret: boolean;
    description: string;
    label: string;
    required: boolean;
    componentProperty: boolean;
    group: string;
    kind: string;
    deprecated: boolean;
    type: string;
    defaultValue: string;
    tags: Array < string >
    ;
};
export type ConfigurationProperties = Array < ConfigurationProperty > ;

export interface Connection extends BaseEntity {
    organization: Organization;
    icon: string;
    connectorId: string;
    connector: Connector;
    description: string;
    options: {};
    userId: string;
    lastUpdated: string;
    organizationId: string;
    createdDate: string;
    configuredProperties: {};
    id: string;
    tags: Array < string >
    ;
    name: string;
};
export type Connections = Array < Connection > ;

export interface Connector extends BaseEntity {
    icon: string;
    description: string;
    connectorGroup: ConnectorGroup;
    configuredProperties: {};
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
    type: string;
};
export type DataShapes = Array < DataShape > ;

export interface Environment extends BaseEntity {
    id: string;
    name: string;
};
export type Environments = Array < Environment > ;

export interface Integration extends BaseEntity {
    stepsDone: Array < string >
    ;
    timesUsed: number;
    users: Array < User >
    ;
    gitRepo: string;
    description: string;
    configuration: string;
    integrationTemplateId: string;
    statusMessage: string;
    token: string;
    integrationTemplate: IntegrationTemplate;
    connections: Array < Connection >
    ;
    userId: string;
    lastUpdated: string;
    createdDate: string;
    desiredStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    currentStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
    steps: Array < Step >
    ;
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
    environments: Array < Environment >
    ;
    users: Array < User >
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

export interface User extends BaseEntity {
    firstName: string;
    username: string;
    organizationId: string;
    lastName: string;
    fullName: string;
    integrations: Array < Integration >
    ;
    roleId: string;
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
    scope: "PARAMETERS" | "CONNECTIVITY";
    errors: Array < Error >
    ;
    status: "OK" | "ERROR" | "UNSUPPORTED";
};
export type Results = Array < Result > ;

export interface FilterOption extends BaseEntity {
    ops: Array < Op >
    ;
    paths: Array < string >
    ;
};
export type FilterOptions = Array < FilterOption > ;

export interface Op extends BaseEntity {
    label: string;
    operator: string;
};
export type Ops = Array < Op > ;

export interface AcquisitionMethod extends BaseEntity {
    icon: string;
    description: string;
    label: string;
    type: "OAUTH1" | "OAUTH2";
};
export type AcquisitionMethods = Array < AcquisitionMethod > ;

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

export interface OAuthApp extends BaseEntity {
    id: string;
    name: string;
    icon: string;
    clientId: string;
    clientSecret: string;
};
export type OAuthApps = Array < OAuthApp > ;

export interface ListResultString extends BaseEntity {
    items: Array < string >
    ;
    totalCount: number;
};
export type ListResultStrings = Array < ListResultString > ;

class TypeFactoryClass {
    createListResult() {
        return <ListResult > {
            items: undefined,
            totalCount: undefined,
        };
    };

    createListResultWithIdObject() {
        return <ListResultWithIdObject > {
            items: undefined,
            totalCount: undefined,
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

    createAcquisition() {
        return <Acquisition > {
            url: undefined,
            type: undefined,
        };
    };

    createAcquisitionRequest() {
        return <AcquisitionRequest > {};
    };

    createAction() {
        return <Action > {
            camelConnectorGAV: undefined,
            connectorId: undefined,
            description: undefined,
            camelConnectorPrefix: undefined,
            outputDataShape: undefined,
            inputDataShape: undefined,
            id: undefined,
            name: undefined,
            definition: undefined,
        };
    };

    createConfigurationProperty() {
        return <ConfigurationProperty > {
            javaType: undefined,
            displayName: undefined,
            secret: undefined,
            description: undefined,
            label: undefined,
            required: undefined,
            componentProperty: undefined,
            group: undefined,
            kind: undefined,
            deprecated: undefined,
            type: undefined,
            defaultValue: undefined,
            tags: undefined,
        };
    };

    createConnection() {
        return <Connection > {
            organization: undefined,
            icon: undefined,
            connectorId: undefined,
            connector: undefined,
            description: undefined,
            options: undefined,
            userId: undefined,
            lastUpdated: undefined,
            organizationId: undefined,
            createdDate: undefined,
            configuredProperties: undefined,
            id: undefined,
            tags: undefined,
            name: undefined,
        };
    };

    createConnector() {
        return <Connector > {
            icon: undefined,
            description: undefined,
            connectorGroup: undefined,
            configuredProperties: undefined,
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
            stepsDone: undefined,
            timesUsed: undefined,
            users: undefined,
            gitRepo: undefined,
            description: undefined,
            configuration: undefined,
            integrationTemplateId: undefined,
            statusMessage: undefined,
            token: undefined,
            integrationTemplate: undefined,
            connections: undefined,
            userId: undefined,
            lastUpdated: undefined,
            createdDate: undefined,
            desiredStatus: undefined,
            currentStatus: undefined,
            steps: undefined,
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
            environments: undefined,
            users: undefined,
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

    createUser() {
        return <User > {
            firstName: undefined,
            username: undefined,
            organizationId: undefined,
            lastName: undefined,
            fullName: undefined,
            integrations: undefined,
            roleId: undefined,
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
            scope: undefined,
            errors: undefined,
            status: undefined,
        };
    };

    createFilterOptions() {
        return <FilterOption> {
            ops: undefined,
            paths: undefined,
        };
    };

    createOp() {
        return <Op > {
            label: undefined,
            operator: undefined,
        };
    };

    createAcquisitionMethod() {
        return <AcquisitionMethod > {
            icon: undefined,
            description: undefined,
            label: undefined,
            type: undefined,
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
            items: undefined,
            totalCount: undefined,
        };
    };

};

export const TypeFactory = new TypeFactoryClass();
