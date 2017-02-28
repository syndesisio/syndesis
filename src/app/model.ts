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

export interface Connection extends BaseEntity {
    icon: string;
    organization: Organization;
    tags: Array < Tag >
    ;
    position: string;
    description: string;
    connector: Connector;
    userId: string;
    configuredProperties: string;
    connectorId: string;
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
    steps: Array < Step >
    ;
    integrationTemplateId: string;
    description: string;
    connections: Array < Connection >
    ;
    configuration: string;
    userId: string;
    users: Array < User >
    ;
    integrationTemplate: IntegrationTemplate;
    id: string;
    name: string;
};
export type Integrations = Array < Integration > ;

export interface IntegrationPattern extends BaseEntity {
    icon: string;
    integrationPatternGroup: IntegrationPatternGroup;
    integrationPatternGroupId: string;
    properties: string;
    id: string;
    name: string;
};
export type IntegrationPatterns = Array < IntegrationPattern > ;

export interface IntegrationPatternGroup extends BaseEntity {
    id: string;
    name: string;
};
export type IntegrationPatternGroups = Array < IntegrationPatternGroup > ;

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
    configuredProperties: string;
    integrationPattern: IntegrationPattern;
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

    createConnection() {
        return <Connection > {
            icon: undefined,
            organization: undefined,
            tags: undefined,
            position: undefined,
            description: undefined,
            connector: undefined,
            userId: undefined,
            configuredProperties: undefined,
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
            connectorGroup: undefined,
            connectorGroupId: undefined,
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
            steps: undefined,
            integrationTemplateId: undefined,
            description: undefined,
            connections: undefined,
            configuration: undefined,
            userId: undefined,
            users: undefined,
            integrationTemplate: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createIntegrationPattern() {
        return <IntegrationPattern > {
            icon: undefined,
            integrationPatternGroup: undefined,
            integrationPatternGroupId: undefined,
            properties: undefined,
            id: undefined,
            name: undefined,
        };
    };

    createIntegrationPatternGroup() {
        return <IntegrationPatternGroup > {
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
            configuredProperties: undefined,
            integrationPattern: undefined,
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

    createEventMessage() {
        return <EventMessage > {
            data: undefined,
            event: undefined,
        };
    };

};

export const TypeFactory = new TypeFactoryClass();