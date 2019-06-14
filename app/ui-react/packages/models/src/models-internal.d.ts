export interface APISummary {
  icon?: string;
  description?: string;
  errors?: Violation[];
  warnings?: Violation[];
  actionsSummary?: ActionsSummary;
  name: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
}
export interface AcquisitionMethod {
  icon?: string;
  type?: 'OAUTH1' | 'OAUTH2';
  description?: string;
  label?: string;
}
export interface Action {
  id?: string;
  name: string;
  description?: string;
  descriptor?: ActionDescriptor;
  tags?: string[];
  actionType?: string;
  pattern?: 'From' | 'Pipe' | 'To';
  metadata?: {
    [name: string]: string;
  };
}
export interface ActionDescriptor {
  propertyDefinitionSteps?: ActionDescriptorStep[];
  inputDataShape?: DataShape;
  outputDataShape?: DataShape;
}
export interface ActionDescriptorStep {
  description?: string;
  name: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
}
export interface ActionsSummary {
  actionCountByTags?: {
    [name: string]: number; // int32
  };
  totalActions?: number; // int32
}
export interface ConfigurationProperty {
  raw?: boolean;
  type?: string;
  defaultValue?: string;
  displayName?: string;
  description?: string;
  label?: string;
  enum?: PropertyValue[];
  kind?: string;
  deprecated?: boolean;
  group?: string;
  secret?: boolean;
  multiple?: boolean;
  required?: boolean;
  generator?: string;
  connectorValue?: string;
  labelHint?: string;
  placeholder?: string;
  relation?: PropertyRelation[];
  javaType?: string;
  controlHint?: string;
  componentProperty?: boolean;
  tags?: string[];
  order?: OptionalInt;
}
export interface Connection {
  id?: string;
  icon?: string;
  options?: {
    [name: string]: string;
  };
  description?: string;
  lastUpdated?: string; // date-time
  organization?: Organization;
  userId?: string;
  organizationId?: string;
  connector?: Connector;
  connectorId?: string;
  createdDate?: string; // date-time
  derived?: boolean;
  tags?: string[];
  name: string;
  configuredProperties?: {
    [name: string]: string;
  };
  metadata?: {
    [name: string]: string;
  };
  readonly uses?: number; // int32
}
export interface ConnectionBulletinBoard {
  id?: string;
  errors?: OptionalInt;
  warnings?: OptionalInt;
  notices?: OptionalInt;
  targetResourceId?: string;
  metadata?: {
    [name: string]: string;
  };
  messages?: LeveledMessage[];
  createdAt?: number; // int64
  updatedAt?: number; // int64
}
export interface ConnectionOverview {
  board?: ConnectionBulletinBoard;
  id?: string;
  icon?: string;
  options?: {
    [name: string]: string;
  };
  description?: string;
  lastUpdated?: string; // date-time
  organization?: Organization;
  userId?: string;
  organizationId?: string;
  connector?: Connector;
  connectorId?: string;
  createdDate?: string; // date-time
  derived?: boolean;
  tags?: string[];
  name: string;
  configuredProperties?: {
    [name: string]: string;
  };
  metadata?: {
    [name: string]: string;
  };
  readonly uses?: number; // int32
}
export interface Connector {
  icon?: string;
  description?: string;
  readonly uses?: OptionalInt;
  connectorGroup?: ConnectorGroup;
  connectorGroupId?: string;
  componentScheme?: string;
  connectorFactory?: string;
  connectorCustomizers?: string[];
  actionsSummary?: ActionsSummary;
  id?: string;
  version?: number; // int32
  actions: ConnectorAction[];
  tags?: string[];
  name: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
  dependencies?: Dependency[];
  metadata?: {
    [name: string]: string;
  };
}
export interface ConnectorAction {
  id?: string;
  name: string;
  description?: string;
  descriptor?: ConnectorDescriptor;
  tags?: string[];
  actionType?: string;
  dependencies?: Dependency[];
  pattern?: 'From' | 'Pipe' | 'To';
  metadata?: {
    [name: string]: string;
  };
}
export interface ConnectorDescriptor {
  camelConnectorGAV?: string;
  componentScheme?: string;
  connectorFactory?: string;
  connectorCustomizers?: string[];
  connectorId?: string;
  camelConnectorPrefix?: string;
  propertyDefinitionSteps?: ActionDescriptorStep[];
  inputDataShape?: DataShape;
  outputDataShape?: DataShape;
  configuredProperties?: {
    [name: string]: string;
  };
}
export interface ConnectorGroup {
  id?: string;
  name: string;
}
export interface ConnectorTemplate {
  icon?: string;
  description?: string;
  connectorGroup?: ConnectorGroup;
  componentScheme?: string;
  connectorProperties?: {
    [name: string]: ConfigurationProperty;
  };
  id?: string;
  name: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
}
export interface ContinuousDeliveryEnvironment {
  releaseTag?: string;
  lastExportedAt?: string; // date-time
  lastTaggedAt?: string; // date-time
  lastImportedAt?: string; // date-time
  name: string;
}
export interface DataManager {}
export interface DataShape {
  name: string;
  type?: string;
  variants?: DataShape[];
  description?: string;
  kind?:
    | 'ANY'
    | 'JAVA'
    | 'JSON_SCHEMA'
    | 'JSON_INSTANCE'
    | 'XML_SCHEMA'
    | 'XML_SCHEMA_INSPECTED'
    | 'XML_INSTANCE'
    | 'NONE';
  metadata?: {
    [name: string]: string;
  };
  specification?: string;
  exemplar?: string /* byte */[];
  collectionType?: string;
  collectionClassName?: string;
}
export interface Dependency {
  id?: string;
  type?: 'MAVEN' | 'EXTENSION' | 'EXTENSION_TAG' | 'ICON';
}
export interface Environment {
  id?: string;
  name: string;
}
export interface EventMessage {
  data?: {};
  event?: string;
}
export interface Extension {
  schemaVersion: string;
  name: string;
  description?: string;
  icon?: string;
  extensionId?: string;
  version?: string;
  tags?: string[];
  actions: Action[];
  dependencies?: Dependency[];
  lastUpdated?: string; // date-time
  status?: 'Draft' | 'Installed' | 'Deleted';
  userId?: string;
  createdDate?: string; // date-time
  extensionType: 'Steps' | 'Connectors' | 'Libraries';
  id?: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
  metadata?: {
    [name: string]: string;
  };
  readonly uses?: number; // int32
}
export interface FilterOptions {
  paths?: string[];
  ops?: Op[];
}
export interface Flow {
  type?: 'PRIMARY' | 'API_PROVIDER' | 'ALTERNATE';
  description?: string;
  connections?: Connection[];
  scheduler?: Scheduler;
  name: string;
  id?: string;
  tags?: string[];
  steps?: Step[];
  metadata?: {
    [name: string]: string;
  };
}
export interface ImmutableConnectorAction {
  readonly id?: string;
  readonly name: string;
  readonly description?: string;
  readonly descriptor?: ConnectorDescriptor;
  readonly tags?: string[];
  readonly actionType?: string;
  readonly pattern?: 'From' | 'Pipe' | 'To';
  readonly metadata?: {
    [name: string]: string;
  };
  dependencies?: Dependency[];
}
export interface ImmutableStepAction {
  readonly id?: string;
  readonly name: string;
  readonly description?: string;
  readonly descriptor?: StepDescriptor;
  readonly tags?: string[];
  readonly actionType?: string;
  readonly pattern?: 'From' | 'Pipe' | 'To';
  readonly metadata?: {
    [name: string]: string;
  };
}
export interface Integration {
  id?: string;
  description?: string;
  flows?: Flow[];
  steps?: Step[];
  connections?: Connection[];
  deleted?: boolean;
  continuousDeliveryState?: {
    [name: string]: ContinuousDeliveryEnvironment;
  };
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
  version?: number; // int32
  createdAt?: number; // int64
  updatedAt?: number; // int64
  tags?: string[];
  name: string;
  resources?: ResourceIdentifier[];
}
export interface IntegrationBulletinBoard {
  errors?: OptionalInt;
  warnings?: OptionalInt;
  notices?: OptionalInt;
  targetResourceId?: string;
  id?: string;
  metadata?: {
    [name: string]: string;
  };
  messages?: LeveledMessage[];
  createdAt?: number; // int64
  updatedAt?: number; // int64
}
export interface IntegrationDeployment {
  spec?: Integration;
  integrationId?: string;
  error?: IntegrationDeploymentError;
  userId?: string;
  currentState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  stepsDone?: {
    [name: string]: string;
  };
  statusMessage?: string;
  targetState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  id?: string;
  version?: number; // int32
  createdAt?: number; // int64
  updatedAt?: number; // int64
}
export interface IntegrationDeploymentError {
  message?: string;
  type?: string;
}
export interface IntegrationDeploymentMetrics {
  version?: string;
  start?: string; // date-time
  errors?: number; // int64
  messages?: number; // int64
  lastProcessed?: string; // date-time
}
export interface IntegrationDeploymentOverview {
  error?: IntegrationDeploymentError;
  userId?: string;
  currentState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  stepsDone?: {
    [name: string]: string;
  };
  statusMessage?: string;
  targetState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  id?: string;
  version?: number; // int32
  createdAt?: number; // int64
  updatedAt?: number; // int64
}
export interface IntegrationMetricsSummary {
  start?: string; // date-time
  errors?: number; // int64
  messages?: number; // int64
  lastProcessed?: string; // date-time
  metricsProvider?: string;
  integrationDeploymentMetrics?: IntegrationDeploymentMetrics[];
  topIntegrations?: {
    [name: string]: number; // int64
  };
  id?: string;
}
export interface IntegrationOverview {
  url?: string;
  board?: IntegrationBulletinBoard;
  draft?: boolean;
  currentState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  deploymentVersion?: number; // int32
  targetState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
  deployments?: IntegrationDeploymentOverview[];
  id?: string;
  description?: string;
  flows?: Flow[];
  steps?: Step[];
  connections?: Connection[];
  deleted?: boolean;
  continuousDeliveryState?: {
    [name: string]: ContinuousDeliveryEnvironment;
  };
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
  version?: number; // int32
  createdAt?: number; // int64
  updatedAt?: number; // int64
  tags?: string[];
  name: string;
  resources?: ResourceIdentifier[];
}
export interface LeveledMessage {
  message?: string;
  level?: 'INFO' | 'WARN' | 'ERROR';
  detail?: string;
  code?:
    | 'SYNDESIS000'
    | 'SYNDESIS001'
    | 'SYNDESIS002'
    | 'SYNDESIS003'
    | 'SYNDESIS004'
    | 'SYNDESIS005'
    | 'SYNDESIS006'
    | 'SYNDESIS007'
    | 'SYNDESIS008'
    | 'SYNDESIS009'
    | 'SYNDESIS010'
    | 'SYNDESIS011'
    | 'SYNDESIS012';
  metadata?: {
    [name: string]: string;
  };
}
export interface ListResult {
  items?: {}[];
  totalCount?: number; // int32
}
export interface ListResultConnectionOverview {
  items?: ConnectionOverview[];
  totalCount?: number; // int32
}
export interface ListResultConnector {
  items?: Connector[];
  totalCount?: number; // int32
}
export interface ListResultConnectorAction {
  items?: ConnectorAction[];
  totalCount?: number; // int32
}
export interface ListResultConnectorTemplate {
  items?: ConnectorTemplate[];
  totalCount?: number; // int32
}
export interface ListResultExtension {
  items?: Extension[];
  totalCount?: number; // int32
}
export interface ListResultIntegrationDeployment {
  items?: IntegrationDeployment[];
  totalCount?: number; // int32
}
export interface ListResultIntegrationOverview {
  items?: IntegrationOverview[];
  totalCount?: number; // int32
}
export interface ListResultOAuthApp {
  items?: OAuthApp[];
  totalCount?: number; // int32
}
export interface ListResultString {
  items?: string[];
  totalCount?: number; // int32
}
export interface ListResultWithIdObject {
  items?: WithIdObject[];
  totalCount?: number; // int32
}
export interface ModelData {
  kind?:
    | 'Action'
    | 'Connection'
    | 'ConnectionOverview'
    | 'Connector'
    | 'ConnectorAction'
    | 'ConnectorGroup'
    | 'ConnectorTemplate'
    | 'Icon'
    | 'Environment'
    | 'EnvironmentType'
    | 'Extension'
    | 'StepAction'
    | 'Organization'
    | 'Integration'
    | 'IntegrationOverview'
    | 'IntegrationDeployment'
    | 'IntegrationDeploymentStateDetails'
    | 'IntegrationMetricsSummary'
    | 'IntegrationRuntime'
    | 'IntegrationEndpoint'
    | 'Step'
    | 'Permission'
    | 'Role'
    | 'User'
    | 'ConnectionBulletinBoard'
    | 'IntegrationBulletinBoard'
    | 'OpenApi';
  condition?: string;
  data?: string;
}
export interface ModelDataObject {
  kind?:
    | 'Action'
    | 'Connection'
    | 'ConnectionOverview'
    | 'Connector'
    | 'ConnectorAction'
    | 'ConnectorGroup'
    | 'ConnectorTemplate'
    | 'Icon'
    | 'Environment'
    | 'EnvironmentType'
    | 'Extension'
    | 'StepAction'
    | 'Organization'
    | 'Integration'
    | 'IntegrationOverview'
    | 'IntegrationDeployment'
    | 'IntegrationDeploymentStateDetails'
    | 'IntegrationMetricsSummary'
    | 'IntegrationRuntime'
    | 'IntegrationEndpoint'
    | 'Step'
    | 'Permission'
    | 'Role'
    | 'User'
    | 'ConnectionBulletinBoard'
    | 'IntegrationBulletinBoard'
    | 'OpenApi';
  condition?: string;
  data?: string;
}
export interface OAuthApp {
  icon?: string;
  derived?: boolean;
  id?: string;
  name: string;
  properties?: {
    [name: string]: ConfigurationProperty;
  };
  configuredProperties?: {
    [name: string]: string;
  };
}
export interface Op {
  label?: string;
  operator?: string;
}
export interface OptionalInt {
  present?: boolean;
  asInt?: number; // int32
}
export interface Organization {
  environments?: Environment[];
  users?: User[];
  id?: string;
  name: string;
}
export interface Principal {
  name?: string;
}
export interface PropertyRelation {
  when?: When[];
  action?: string;
}
export interface PropertyValue {
  value?: string;
  label?: string;
}
export interface Quota {
  maxIntegrationsPerUser?: number; // int32
  maxDeploymentsPerUser?: number; // int32
  usedIntegrationsPerUser?: number; // int32
  usedDeploymentsPerUser?: number; // int32
}
export interface Reservation {
  principal?: Principal;
  createdAt?: number; // int64
}
export interface ResourceIdentifier {
  version?: number; // int32
  kind?:
    | 'Action'
    | 'Connection'
    | 'ConnectionOverview'
    | 'Connector'
    | 'ConnectorAction'
    | 'ConnectorGroup'
    | 'ConnectorTemplate'
    | 'Icon'
    | 'Environment'
    | 'EnvironmentType'
    | 'Extension'
    | 'StepAction'
    | 'Organization'
    | 'Integration'
    | 'IntegrationOverview'
    | 'IntegrationDeployment'
    | 'IntegrationDeploymentStateDetails'
    | 'IntegrationMetricsSummary'
    | 'IntegrationRuntime'
    | 'IntegrationEndpoint'
    | 'Step'
    | 'Permission'
    | 'Role'
    | 'User'
    | 'ConnectionBulletinBoard'
    | 'IntegrationBulletinBoard'
    | 'OpenApi';
  id?: string;
}
export interface Result {
  errors?: VerifierError[];
  scope?: 'PARAMETERS' | 'CONNECTIVITY';
  status?: 'OK' | 'ERROR' | 'UNSUPPORTED';
}
export interface Scheduler {
  type?: 'timer' | 'cron';
  expression?: string;
}
export interface Step {
  name?: string;
  extension?: Extension;
  connection?: Connection;
  action?: Action;
  stepKind?:
    | 'endpoint'
    | 'connector'
    | 'expressionFilter'
    | 'ruleFilter'
    | 'extension'
    | 'mapper'
    | 'choice'
    | 'split'
    | 'aggregate'
    | 'log'
    | 'headers'
    | 'template';
  id?: string;
  configuredProperties?: {
    [name: string]: string;
  };
  dependencies?: Dependency[];
  metadata?: {
    [name: string]: string;
  };
}
export interface StepDescriptor {
  resource?: string;
  kind?: 'STEP' | 'BEAN' | 'ENDPOINT';
  entrypoint?: string;
  propertyDefinitionSteps?: ActionDescriptorStep[];
  inputDataShape?: DataShape;
  outputDataShape?: DataShape;
}
export interface StreamingOutput {}
export interface TargetStateRequest {
  targetState?: 'Published' | 'Unpublished' | 'Error' | 'Pending';
}
export interface User {
  fullName?: string;
  name?: string;
  username?: string;
  organizationId?: string;
  integrations?: Integration[];
  roleId?: string;
  lastName?: string;
  firstName?: string;
  id?: string;
}
export interface Validator {}
export interface VerifierError {
  parameters?: string[];
  attributes?: {
    [name: string]: {};
  };
  description?: string;
  code?: string;
}
export interface Violation {}
export interface When {
  value?: string;
  id?: string;
}
export interface WithId {
  id?: string;
}
export interface WithIdObject {
  id?: string;
}
export interface WithResourceId {
  id?: string;
}
