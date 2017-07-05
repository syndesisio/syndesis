/* tslint:disable */

export interface BaseEntity {
  readonly id?: string;
  // TODO we'll make this optional for now
  kind?: string;
}

export interface ListResult extends BaseEntity {
  items: Array<{}>;
  totalCount: number;
}
export type ListResults = Array<ListResult>;

export interface ListResultWithId extends BaseEntity {
  items: Array<WithId>;
  totalCount: number;
}
export type ListResultWithIds = Array<ListResultWithId>;

export interface WithId extends BaseEntity {
  id: string;
}
export type WithIds = Array<WithId>;

export interface Action extends BaseEntity {
  outputDataShape: DataShape;
  description: string;
  inputDataShape: DataShape;
  camelConnectorPrefix: string;
  connectorId: string;
  camelConnectorGAV: string;
  id: string;
  name: string;
  properties: {};
}
export type Actions = Array<Action>;

export interface ConfigurationProperty extends BaseEntity {
  deprecated: boolean;
  javaType: string;
  displayName: string;
  componentProperty: boolean;
  kind: string;
  description: string;
  required: boolean;
  secret: boolean;
  label: string;
  group: string;
  type: string;
  defaultValue: string;
}
export type ConfigurationProperties = Array<ConfigurationProperty>;

export interface Connection extends BaseEntity {
  icon: string;
  organization: Organization;
  options: {};
  description: string;
  connector: Connector;
  connectorId: string;
  organizationId: string;
  userId: string;
  configuredProperties: {};
  lastUpdated: string;
  createdDate: string;
  id: string;
  tags: Array<string>;
  name: string;
}
export type Connections = Array<Connection>;

export interface Connector extends BaseEntity {
  connectorGroupId: string;
  icon: string;
  description: string;
  connectorGroup: ConnectorGroup;
  properties: {};
  actions: Array<Action>;
  id: string;
  name: string;
}
export type Connectors = Array<Connector>;

export interface ConnectorGroup extends BaseEntity {
  id: string;
  name: string;
}
export type ConnectorGroups = Array<ConnectorGroup>;

export interface DataShape extends BaseEntity {
  exemplar: Array<string>;
  kind: string;
  type: string;
}
export type DataShapes = Array<DataShape>;

export interface Environment extends BaseEntity {
  id: string;
  name: string;
}
export type Environments = Array<Environment>;

export interface Integration extends BaseEntity {
  connections: Array<Connection>;
  users: Array<User>;
  description: string;
  desiredStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
  currentStatus: "Draft" | "Pending" | "Activated" | "Deactivated" | "Deleted";
  statusMessage: string;
  configuration: string;
  token: string;
  gitRepo: string;
  steps: Array<Step>;
  stepsDone: Array<string>;
  integrationTemplateId: string;
  integrationTemplate: IntegrationTemplate;
  userId: string;
  lastUpdated: string;
  createdDate: string;
  timesUsed: number;
  id: string;
  tags: Array<string>;
  name: string;
}
export type Integrations = Array<Integration>;

export interface IntegrationTemplate extends BaseEntity {
  organization: Organization;
  configuration: string;
  organizationId: string;
  userId: string;
  id: string;
  name: string;
}
export type IntegrationTemplates = Array<IntegrationTemplate>;

export interface Organization extends BaseEntity {
  environments: Array<Environment>;
  users: Array<User>;
  id: string;
  name: string;
}
export type Organizations = Array<Organization>;

export interface Step extends BaseEntity {
  connection: Connection;
  action: Action;
  configuredProperties: {};
  stepKind: string;
  name: string;
  id: string;
}
export type Steps = Array<Step>;

export interface User extends BaseEntity {
  firstName: string;
  fullName: string;
  username: string;
  integrations: Array<Integration>;
  roleId: string;
  organizationId: string;
  lastName: string;
  name: string;
  id: string;
}
export type Users = Array<User>;

export interface Error extends BaseEntity {
  description: string;
  code: string;
  parameters: Array<string>;
  attributes: {};
}
export type Errors = Array<Error>;

export interface Result extends BaseEntity {
  errors: Array<Error>;
  scope: "PARAMETERS" | "CONNECTIVITY";
  status: "OK" | "ERROR" | "UNSUPPORTED";
}
export type Results = Array<Result>;

export interface ListResultAction extends BaseEntity {
  items: Array<Action>;
  totalCount: number;
}
export type ListResultActions = Array<ListResultAction>;

export interface EventMessage extends BaseEntity {
  data: {};
  event: string;
}
export type EventMessages = Array<EventMessage>;

export interface ListResultString extends BaseEntity {
  items: Array<string>;
  totalCount: number;
}
export type ListResultStrings = Array<ListResultString>;

class TypeFactoryClass {
  createListResult() {
    return <ListResult>{
      items: undefined,
      totalCount: undefined
    };
  }

  createListResultWithId() {
    return <ListResultWithId>{
      items: undefined,
      totalCount: undefined
    };
  }

  createWithId() {
    return <WithId>{
      id: undefined
    };
  }

  createAction() {
    return <Action>{
      outputDataShape: undefined,
      description: undefined,
      inputDataShape: undefined,
      camelConnectorPrefix: undefined,
      connectorId: undefined,
      camelConnectorGAV: undefined,
      id: undefined,
      name: undefined,
      properties: undefined
    };
  }

  createConfigurationProperty() {
    return <ConfigurationProperty>{
      deprecated: undefined,
      javaType: undefined,
      displayName: undefined,
      componentProperty: undefined,
      kind: undefined,
      description: undefined,
      required: undefined,
      secret: undefined,
      label: undefined,
      group: undefined,
      type: undefined,
      defaultValue: undefined
    };
  }

  createConnection() {
    return <Connection>{
      icon: undefined,
      organization: undefined,
      options: undefined,
      description: undefined,
      connector: undefined,
      connectorId: undefined,
      organizationId: undefined,
      userId: undefined,
      configuredProperties: undefined,
      lastUpdated: undefined,
      createdDate: undefined,
      id: undefined,
      tags: undefined,
      name: undefined
    };
  }

  createConnector() {
    return <Connector>{
      connectorGroupId: undefined,
      icon: undefined,
      description: undefined,
      connectorGroup: undefined,
      properties: undefined,
      actions: undefined,
      id: undefined,
      name: undefined
    };
  }

  createConnectorGroup() {
    return <ConnectorGroup>{
      id: undefined,
      name: undefined
    };
  }

  createDataShape() {
    return <DataShape>{
      exemplar: undefined,
      kind: undefined,
      type: undefined
    };
  }

  createEnvironment() {
    return <Environment>{
      id: undefined,
      name: undefined
    };
  }

  createIntegration() {
    return <Integration>{
      connections: undefined,
      users: undefined,
      description: undefined,
      desiredStatus: undefined,
      currentStatus: undefined,
      statusMessage: undefined,
      configuration: undefined,
      token: undefined,
      gitRepo: undefined,
      steps: undefined,
      stepsDone: undefined,
      integrationTemplateId: undefined,
      integrationTemplate: undefined,
      userId: undefined,
      lastUpdated: undefined,
      createdDate: undefined,
      timesUsed: undefined,
      id: undefined,
      tags: undefined,
      name: undefined
    };
  }

  createIntegrationTemplate() {
    return <IntegrationTemplate>{
      organization: undefined,
      configuration: undefined,
      organizationId: undefined,
      userId: undefined,
      id: undefined,
      name: undefined
    };
  }

  createOrganization() {
    return <Organization>{
      environments: undefined,
      users: undefined,
      id: undefined,
      name: undefined
    };
  }

  createStep() {
    return <Step>{
      connection: undefined,
      action: undefined,
      configuredProperties: undefined,
      stepKind: undefined,
      name: undefined,
      id: undefined
    };
  }

  createUser() {
    return <User>{
      firstName: undefined,
      fullName: undefined,
      username: undefined,
      integrations: undefined,
      roleId: undefined,
      organizationId: undefined,
      lastName: undefined,
      name: undefined,
      id: undefined
    };
  }

  createError() {
    return <Error>{
      description: undefined,
      code: undefined,
      parameters: undefined,
      attributes: undefined
    };
  }

  createResult() {
    return <Result>{
      errors: undefined,
      scope: undefined,
      status: undefined
    };
  }

  createListResultAction() {
    return <ListResultAction>{
      items: undefined,
      totalCount: undefined
    };
  }

  createEventMessage() {
    return <EventMessage>{
      data: undefined,
      event: undefined
    };
  }

  createListResultString() {
    return <ListResultString>{
      items: undefined,
      totalCount: undefined
    };
  }
}

export const TypeFactory = new TypeFactoryClass();
