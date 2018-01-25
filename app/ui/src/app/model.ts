
export interface BaseEntity {
  readonly id?: string;
  // TODO we'll make this optional for now
  kind?: string;
}

export interface Exchange extends BaseEntity {
  logts?: string;
  at: number;
  pod: string;
  ver: string;
  status: string;
  failed: boolean;
  steps?: ExchangeStep[];
  metadata?: any;
}

export interface ExchangeStep extends BaseEntity {
  at: number;
  duration?: number;
  failure?: string;
  message?: string[];
  events?: any;
}

// TODO local hack to avoid deleting all the related code
/* tslint:disable */
export interface IntegrationTemplate extends BaseEntity {}
/* tslint:enable */
export type IntegrationTemplates = Array<IntegrationTemplate>;

export interface Extension extends BaseEntity {
  name: string;
  description: string;
  icon: string;
  extensionId: string;
  version: string;
  tags: Array<string>;
  actions: Array<Action>;
  dependencies: Array<string>;
  status: 'Draft' | 'Installed' | 'Deleted';
  id: string;
  schemaVersion: string;
  properties: {};
  configuredProperties: {};
  extensionType: string;
}
export type Extensions = Array<Extension>;

export interface Action extends BaseEntity {
  actionType: string;
  pattern: 'From' | 'To';
  // TODO migrate this to ActionDescriptor
  descriptor: ActionDefinition;
  connectorId: string;
  description: string;
  id: string;
  name: string;
  tags: Array<string>;
}
export type Actions = Array<Action>;

export interface ActionDescriptor extends BaseEntity {
  propertyDefinitionSteps: Array<ActionDescriptorStep>;
  inputDataShape: DataShape;
  outputDataShape: DataShape;
}
export type ActionDescriptors = Array<ActionDescriptor>;

export interface ActionDescriptorStep extends BaseEntity {
  description: string;
  name: string;
  configuredProperties: {};
  properties: {};
}
export type ActionDescriptorSteps = Array<ActionDescriptorStep>;

// TODO deprecate should be ActionDescriptor
export interface ActionDefinition extends BaseEntity {
  camelConnectorGAV: string;
  camelConnectorPrefix: string;
  outputDataShape: DataShape;
  inputDataShape: DataShape;
  propertyDefinitionSteps: Array<ActionDefinitionStep>;
}
export type ActionDefinitions = Array<ActionDefinition>;

// TODO deprecate should be ActionDescriptorStep
export interface ActionDefinitionStep extends BaseEntity {
  description: string;
  name: string;
  properties: {};
  configuredProperties: {};
}
export type ActionDefinitionSteps = Array<ActionDefinitionStep>;

export interface ConfigurationProperty extends BaseEntity {
  javaType: string;
  type: string;
  defaultValue: string;
  displayName: string;
  kind: string;
  description: string;
  group: string;
  required: boolean;
  secret: boolean;
  label: string;
  enum: Array<PropertyValue>;
  componentProperty: boolean;
  deprecated: boolean;
  tags: Array<string>;
}
export type ConfigurationProperties = Array<ConfigurationProperty>;

export interface Connection extends BaseEntity {
  icon: string;
  organization: Organization;
  configuredProperties: {};
  organizationId: string;
  connectorId: string;
  options: {};
  description: string;
  connector: Connector;
  derived: boolean;
  userId: string;
  lastUpdated: string;
  createdDate: string;
  id: string;
  tags: Array<string>;
  name: string;
}
export type Connections = Array<Connection>;

export interface Connector extends BaseEntity {
  icon: string;
  properties: {};
  actions: Array<Action>;
  connectorGroupId: string;
  configuredProperties: {};
  description: string;
  connectorGroup: ConnectorGroup;
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
  specification: string;
  exemplar: Array<string>;
  type: string;
  kind: string;
}
export type DataShapes = Array<DataShape>;

export interface Environment extends BaseEntity {
  id: string;
  name: string;
}
export type Environments = Array<Environment>;

export interface Organization extends BaseEntity {
  environments: Array<Environment>;
  users: Array<User>;
  id: string;
  name: string;
}
export type Organizations = Array<Organization>;

export interface PropertyValue extends BaseEntity {
  value: string;
  label: string;
}
export type PropertyValues = Array<PropertyValue>;

export interface User extends BaseEntity {
  fullName: string;
  name: string;
  organizationId: string;
  username: string;
  firstName: string;
  lastName: string;
  // TODO
  //integrations: Array<Integration>;
  roleId: string;
  id: string;
}
export type Users = Array<User>;

export interface ListResult extends BaseEntity {
  items: Array<{}>;
  totalCount: number;
}
export type ListResults = Array<ListResult>;

export interface ListResultWithIdObject extends BaseEntity {
  items: Array<WithIdObject>;
  totalCount: number;
}
export type ListResultWithIdObjects = Array<ListResultWithIdObject>;

export interface WithId extends BaseEntity {
  id: string;
}
export type WithIds = Array<WithId>;

export interface WithIdObject extends BaseEntity {
  id: string;
}
export type WithIdObjects = Array<WithIdObject>;

/* tslint:disable */
export interface Violation extends BaseEntity {}
/* tslint:enable */
export type Violations = Array<Violation>;

export interface ListResultAction extends BaseEntity {
  items: Array<Action>;
  totalCount: number;
}
export type ListResultActions = Array<ListResultAction>;

export interface AcquisitionMethod extends BaseEntity {
  icon: string;
  type: 'OAUTH1' | 'OAUTH2';
  description: string;
  label: string;
}
export type AcquisitionMethods = Array<AcquisitionMethod>;

export interface AcquisitionRequest extends BaseEntity {
  returnUrl: string;
}
export type AcquisitionRequests = Array<AcquisitionRequest>;

export interface Error extends BaseEntity {
  parameters: Array<string>;
  attributes: {};
  description: string;
  code: string;
}
export type Errors = Array<Error>;

export interface Result extends BaseEntity {
  scope: 'PARAMETERS' | 'CONNECTIVITY';
  errors: Array<Error>;
  status: 'OK' | 'ERROR' | 'UNSUPPORTED';
}
export type Results = Array<Result>;

export interface FilterOptions extends BaseEntity {
  paths: Array<string>;
  ops: Array<Op>;
}
export type FilterOptionss = Array<FilterOptions>;

export interface Op extends BaseEntity {
  label: string;
  operator: string;
}
export type Ops = Array<Op>;

export interface EventMessage extends BaseEntity {
  data: {};
  event: string;
}
export type EventMessages = Array<EventMessage>;

export interface OAuthApp extends BaseEntity {
  id: string;
  name: string;
  icon: string;
  clientId: string;
  clientSecret: string;
}
export type OAuthApps = Array<OAuthApp>;

export interface ListResultString extends BaseEntity {
  items: Array<string>;
  totalCount: number;
}
export type ListResultStrings = Array<ListResultString>;

class TypeFactoryClass {
  createAction() {
    return <Action>{
      descriptor: undefined,
      connectorId: undefined,
      description: undefined,
      id: undefined,
      name: undefined,
      tags: undefined
    };
  }

  createActionDefinition() {
    return <ActionDefinition>{
      outputDataShape: undefined,
      propertyDefinitionSteps: undefined,
      inputDataShape: undefined
    };
  }

  createActionDefinitionStep() {
    return <ActionDefinitionStep>{
      description: undefined,
      name: undefined,
      properties: undefined,
      configuredProperties: undefined
    };
  }

  createConfigurationProperty() {
    return <ConfigurationProperty>{
      javaType: undefined,
      type: undefined,
      defaultValue: undefined,
      displayName: undefined,
      kind: undefined,
      description: undefined,
      group: undefined,
      required: undefined,
      secret: undefined,
      label: undefined,
      enum: undefined,
      componentProperty: undefined,
      deprecated: undefined,
      tags: undefined
    };
  }

  createConnection() {
    return <Connection>{
      icon: undefined,
      organization: undefined,
      configuredProperties: undefined,
      organizationId: undefined,
      connectorId: undefined,
      options: undefined,
      description: undefined,
      connector: undefined,
      derived: undefined,
      userId: undefined,
      lastUpdated: undefined,
      createdDate: undefined,
      id: undefined,
      tags: undefined,
      name: undefined
    };
  }

  createConnector() {
    return <Connector>{
      icon: undefined,
      properties: undefined,
      actions: undefined,
      connectorGroupId: undefined,
      configuredProperties: undefined,
      description: undefined,
      connectorGroup: undefined,
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
      specification: undefined,
      exemplar: undefined,
      type: undefined,
      kind: undefined
    };
  }

  createEnvironment() {
    return <Environment>{
      id: undefined,
      name: undefined
    };
  }

  createExtension() {
    return <Extension>{
      name: undefined,
      description: undefined,
      icon: undefined,
      extensionId: undefined,
      version: undefined,
      tags: undefined,
      actions: undefined,
      dependencies: undefined,
      status: undefined,
      id: undefined,
      properties: undefined
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

  createPropertyValue() {
    return <PropertyValue>{
      value: undefined,
      label: undefined
    };
  }

  createUser() {
    return <User>{
      fullName: undefined,
      name: undefined,
      organizationId: undefined,
      username: undefined,
      firstName: undefined,
      lastName: undefined,
      integrations: undefined,
      roleId: undefined,
      id: undefined
    };
  }

  createListResult() {
    return <ListResult>{
      items: undefined,
      totalCount: undefined
    };
  }

  createListResultWithIdObject() {
    return <ListResultWithIdObject>{
      items: undefined,
      totalCount: undefined
    };
  }

  createWithId() {
    return <WithId>{
      id: undefined
    };
  }

  createWithIdObject() {
    return <WithIdObject>{
      id: undefined
    };
  }

  createViolation() {
    return <Violation>{};
  }

  createListResultAction() {
    return <ListResultAction>{
      items: undefined,
      totalCount: undefined
    };
  }

  createAcquisitionMethod() {
    return <AcquisitionMethod>{
      icon: undefined,
      type: undefined,
      description: undefined,
      label: undefined
    };
  }

  createAcquisitionRequest() {
    return <AcquisitionRequest>{
      returnUrl: undefined
    };
  }

  createError() {
    return <Error>{
      parameters: undefined,
      attributes: undefined,
      description: undefined,
      code: undefined
    };
  }

  createResult() {
    return <Result>{
      scope: undefined,
      errors: undefined,
      status: undefined
    };
  }

  createFilterOptions() {
    return <FilterOptions>{
      paths: undefined,
      ops: undefined
    };
  }

  createOp() {
    return <Op>{
      label: undefined,
      operator: undefined
    };
  }

  createEventMessage() {
    return <EventMessage>{
      data: undefined,
      event: undefined
    };
  }

  createOAuthApp() {
    return <OAuthApp>{
      id: undefined,
      name: undefined,
      icon: undefined,
      clientId: undefined,
      clientSecret: undefined
    };
  }

  createListResultString() {
    return <ListResultString>{
      items: undefined,
      totalCount: undefined
    };
  }
}

/* tslint:disable */
// TODO change this to suit lint
export const TypeFactory = new TypeFactoryClass();
