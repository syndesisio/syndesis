import {
  BaseEntity,
  User, Action,
  ActionDefinition, ListResultAction, ActionDefinitionStep,
  Connector, Connection,
  Organization,
  DataShape
} from '@syndesis/ui/platform';

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
export type IntegrationTemplate = BaseEntity;
export type IntegrationTemplates = Array<IntegrationTemplate>;

export interface Extension extends BaseEntity {
  description: string;
  icon: string;
  extensionId: string;
  version: string;
  tags: Array<string>;
  actions: Array<Action>;
  dependencies: Array<string>;
  status: 'Draft' | 'Installed' | 'Deleted';
  schemaVersion: string;
  properties: {};
  configuredProperties: {};
  extensionType: string;
}
export type Extensions = Array<Extension>;

export interface ConfigurationProperty extends BaseEntity {
  javaType: string;
  type: string;
  defaultValue: string;
  displayName: string;
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

export interface PropertyValue extends BaseEntity {
  value: string;
  label: string;
}

export type PropertyValues = Array<PropertyValue>;

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

export type WithId = BaseEntity;

export type WithIds = Array<WithId>;

export type WithIdObject = BaseEntity;

export type WithIdObjects = Array<WithIdObject>;

export type Violation = BaseEntity;
export type Violations = Array<Violation>;

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

export class TypeFactory {
  static create<T>(): T {
    return {} as T;
  }

  static createAction() {
    return <Action>{
      descriptor: undefined,
      connectorId: undefined,
      description: undefined,
      id: undefined,
      name: undefined,
      tags: undefined
    };
  }

  static createActionDefinition() {
    return <ActionDefinition>{
      outputDataShape: undefined,
      propertyDefinitionSteps: undefined,
      inputDataShape: undefined
    };
  }

  static createActionDefinitionStep() {
    return <ActionDefinitionStep>{
      description: undefined,
      name: undefined,
      properties: undefined,
      configuredProperties: undefined
    };
  }

  static createConfigurationProperty() {
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

  static createConnection() {
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

  static createConnector() {
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

  static createConnectorGroup() {
    return <BaseEntity>{
      id: undefined,
      name: undefined
    };
  }

  static createDataShape() {
    return <DataShape>{
      specification: undefined,
      exemplar: undefined,
      type: undefined,
      kind: undefined
    };
  }

  static createEnvironment() {
    return <BaseEntity>{
      id: undefined,
      name: undefined
    };
  }

  static createExtension() {
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

  static createOrganization() {
    return <Organization>{
      environments: undefined,
      users: undefined,
      id: undefined,
      name: undefined
    };
  }

  static createPropertyValue() {
    return <PropertyValue>{
      value: undefined,
      label: undefined
    };
  }

  static createUser() {
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

  static createListResult() {
    return <ListResult>{
      items: undefined,
      totalCount: undefined
    };
  }

  static createListResultWithIdObject() {
    return <ListResultWithIdObject>{
      items: undefined,
      totalCount: undefined
    };
  }

  static createWithId() {
    return <WithId>{
      id: undefined
    };
  }

  static createWithIdObject() {
    return <WithIdObject>{
      id: undefined
    };
  }

  static createViolation() {
    return <Violation>{};
  }

  static createListResultAction() {
    return <ListResultAction>{
      items: undefined,
      totalCount: undefined
    };
  }

  static createAcquisitionMethod() {
    return <AcquisitionMethod>{
      icon: undefined,
      type: undefined,
      description: undefined,
      label: undefined
    };
  }

  static createAcquisitionRequest() {
    return <AcquisitionRequest>{
      returnUrl: undefined
    };
  }

  static createError() {
    return <Error>{
      parameters: undefined,
      attributes: undefined,
      description: undefined,
      code: undefined
    };
  }

  static createResult() {
    return <Result>{
      scope: undefined,
      errors: undefined,
      status: undefined
    };
  }

  static createFilterOptions() {
    return <FilterOptions>{
      paths: undefined,
      ops: undefined
    };
  }

  static createOp() {
    return <Op>{
      label: undefined,
      operator: undefined
    };
  }

  static createEventMessage() {
    return <EventMessage>{
      data: undefined,
      event: undefined
    };
  }

  static createOAuthApp() {
    return <OAuthApp>{
      id: undefined,
      name: undefined,
      icon: undefined,
      clientId: undefined,
      clientSecret: undefined
    };
  }

  static createListResultString() {
    return <ListResultString>{
      items: undefined,
      totalCount: undefined
    };
  }
}
