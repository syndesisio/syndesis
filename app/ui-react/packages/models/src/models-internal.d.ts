export interface APISummary {
    actionsSummary?: ActionsSummary;
    icon?: string;
    description?: string;
    errors?: Violation[];
    warnings?: Violation[];
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
    type?: "OAUTH1" | "OAUTH2";
    label?: string;
    description?: string;
}
export interface Action {
    id?: string;
    name: string;
    description?: string;
    descriptor?: ActionDescriptor;
    tags?: string[];
    actionType?: string;
    pattern?: "From" | "Pipe" | "To";
    metadata?: {
        [name: string]: string;
    };
}
export interface ActionDescriptor {
    propertyDefinitionSteps?: ActionDescriptorStep[];
    outputDataShape?: DataShape;
    inputDataShape?: DataShape;
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
    javaType?: string;
    componentProperty?: boolean;
    connectorValue?: string;
    controlHint?: string;
    labelHint?: string;
    placeholder?: string;
    relation?: PropertyRelation[];
    required?: boolean;
    multiple?: boolean;
    generator?: string;
    secret?: boolean;
    raw?: boolean;
    type?: string;
    defaultValue?: string;
    displayName?: string;
    label?: string;
    description?: string;
    deprecated?: boolean;
    group?: string;
    kind?: string;
    enum?: PropertyValue[];
    tags?: string[];
    order?: OptionalInt;
}
export interface Connection {
    id?: string;
    connectorId?: string;
    createdDate?: string; // date-time
    organizationId?: string;
    derived?: boolean;
    connector?: Connector;
    icon?: string;
    options?: {
        [name: string]: string;
    };
    description?: string;
    organization?: Organization;
    lastUpdated?: string; // date-time
    userId?: string;
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
    targetResourceId?: string;
    notices?: OptionalInt;
    errors?: OptionalInt;
    warnings?: OptionalInt;
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
    connectorId?: string;
    createdDate?: string; // date-time
    organizationId?: string;
    derived?: boolean;
    connector?: Connector;
    icon?: string;
    options?: {
        [name: string]: string;
    };
    description?: string;
    organization?: Organization;
    lastUpdated?: string; // date-time
    userId?: string;
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
    connectorGroup?: ConnectorGroup;
    connectorGroupId?: string;
    componentScheme?: string;
    connectorFactory?: string;
    connectorCustomizers?: string[];
    actionsSummary?: ActionsSummary;
    icon?: string;
    description?: string;
    readonly uses?: OptionalInt;
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
    pattern?: "From" | "Pipe" | "To";
    metadata?: {
        [name: string]: string;
    };
}
export interface ConnectorDescriptor {
    connectorId?: string;
    camelConnectorGAV?: string;
    camelConnectorPrefix?: string;
    componentScheme?: string;
    connectorFactory?: string;
    connectorCustomizers?: string[];
    propertyDefinitionSteps?: ActionDescriptorStep[];
    outputDataShape?: DataShape;
    inputDataShape?: DataShape;
    configuredProperties?: {
        [name: string]: string;
    };
}
export interface ConnectorGroup {
    id?: string;
    name: string;
}
export interface ConnectorTemplate {
    connectorProperties?: {
        [name: string]: ConfigurationProperty;
    };
    connectorGroup?: ConnectorGroup;
    componentScheme?: string;
    icon?: string;
    description?: string;
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
    lastTaggedAt?: string; // date-time
    releaseTag?: string;
    lastExportedAt?: string; // date-time
    lastImportedAt?: string; // date-time
    name: string;
}
export interface DataManager {
}
export interface DataShape {
    collectionType?: string;
    specification?: string;
    collectionClassName?: string;
    exemplar?: string /* byte */ [];
    name: string;
    type?: string;
    variants?: DataShape[];
    description?: string;
    metadata?: {
        [name: string]: string;
    };
    kind?: "ANY" | "JAVA" | "JSON_SCHEMA" | "JSON_INSTANCE" | "XML_SCHEMA" | "XML_SCHEMA_INSPECTED" | "XML_INSTANCE" | "NONE";
}
export interface Dependency {
    id?: string;
    type?: "MAVEN" | "EXTENSION" | "EXTENSION_TAG" | "ICON";
}
export interface Environment {
    id?: string;
    name: string;
}
export interface EventMessage {
    event?: string;
    data?: {
    };
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
    extensionType: "Steps" | "Connectors" | "Libraries";
    createdDate?: string; // date-time
    lastUpdated?: string; // date-time
    status?: "Draft" | "Installed" | "Deleted";
    userId?: string;
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
    ops?: Op[];
    paths?: string[];
}
export interface Flow {
    connections?: Connection[];
    scheduler?: Scheduler;
    description?: string;
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
    readonly pattern?: "From" | "Pipe" | "To";
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
    readonly pattern?: "From" | "Pipe" | "To";
    readonly metadata?: {
        [name: string]: string;
    };
}
export interface Integration {
    id?: string;
    continuousDeliveryState?: {
        [name: string]: ContinuousDeliveryEnvironment;
    };
    flows?: Flow[];
    connections?: Connection[];
    deleted?: boolean;
    description?: string;
    steps?: Step[];
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
    targetResourceId?: string;
    notices?: OptionalInt;
    errors?: OptionalInt;
    warnings?: OptionalInt;
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
    stepsDone?: {
        [name: string]: string;
    };
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
    statusMessage?: string;
    error?: IntegrationDeploymentError;
    userId?: string;
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
    lastProcessed?: string; // date-time
    version?: string;
    errors?: number; // int64
    messages?: number; // int64
    start?: string; // date-time
}
export interface IntegrationDeploymentOverview {
    stepsDone?: {
        [name: string]: string;
    };
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
    statusMessage?: string;
    error?: IntegrationDeploymentError;
    userId?: string;
    id?: string;
    version?: number; // int32
    createdAt?: number; // int64
    updatedAt?: number; // int64
}
export interface IntegrationMetricsSummary {
    metricsProvider?: string;
    integrationDeploymentMetrics?: IntegrationDeploymentMetrics[];
    topIntegrations?: {
        [name: string]: number; // int64
    };
    lastProcessed?: string; // date-time
    errors?: number; // int64
    messages?: number; // int64
    start?: string; // date-time
    id?: string;
}
export interface IntegrationOverview {
    draft?: boolean;
    deployments?: IntegrationDeploymentOverview[];
    board?: IntegrationBulletinBoard;
    deploymentVersion?: number; // int32
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
    url?: string;
    id?: string;
    continuousDeliveryState?: {
        [name: string]: ContinuousDeliveryEnvironment;
    };
    flows?: Flow[];
    connections?: Connection[];
    deleted?: boolean;
    description?: string;
    steps?: Step[];
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
    level?: "INFO" | "WARN" | "ERROR";
    detail?: string;
    code?: "SYNDESIS000" | "SYNDESIS001" | "SYNDESIS002" | "SYNDESIS003" | "SYNDESIS004" | "SYNDESIS005" | "SYNDESIS006" | "SYNDESIS007" | "SYNDESIS008" | "SYNDESIS009" | "SYNDESIS010" | "SYNDESIS011" | "SYNDESIS012";
    metadata?: {
        [name: string]: string;
    };
}
export interface ListResult {
    items?: {
    }[];
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
    kind?: "Action" | "Connection" | "ConnectionOverview" | "Connector" | "ConnectorAction" | "ConnectorGroup" | "ConnectorTemplate" | "Icon" | "Environment" | "EnvironmentType" | "Extension" | "StepAction" | "Organization" | "Integration" | "IntegrationOverview" | "IntegrationDeployment" | "IntegrationDeploymentStateDetails" | "IntegrationMetricsSummary" | "IntegrationRuntime" | "IntegrationEndpoint" | "Step" | "Permission" | "Role" | "User" | "ConnectionBulletinBoard" | "IntegrationBulletinBoard" | "OpenApi";
    data?: string;
}
export interface ModelDataObject {
    kind?: "Action" | "Connection" | "ConnectionOverview" | "Connector" | "ConnectorAction" | "ConnectorGroup" | "ConnectorTemplate" | "Icon" | "Environment" | "EnvironmentType" | "Extension" | "StepAction" | "Organization" | "Integration" | "IntegrationOverview" | "IntegrationDeployment" | "IntegrationDeploymentStateDetails" | "IntegrationMetricsSummary" | "IntegrationRuntime" | "IntegrationEndpoint" | "Step" | "Permission" | "Role" | "User" | "ConnectionBulletinBoard" | "IntegrationBulletinBoard" | "OpenApi";
    data?: string;
}
export interface OAuthApp {
    derived?: boolean;
    icon?: string;
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
    action?: string;
    when?: When[];
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
    kind?: "Action" | "Connection" | "ConnectionOverview" | "Connector" | "ConnectorAction" | "ConnectorGroup" | "ConnectorTemplate" | "Icon" | "Environment" | "EnvironmentType" | "Extension" | "StepAction" | "Organization" | "Integration" | "IntegrationOverview" | "IntegrationDeployment" | "IntegrationDeploymentStateDetails" | "IntegrationMetricsSummary" | "IntegrationRuntime" | "IntegrationEndpoint" | "Step" | "Permission" | "Role" | "User" | "ConnectionBulletinBoard" | "IntegrationBulletinBoard" | "OpenApi";
    id?: string;
}
export interface Result {
    errors?: VerifierError[];
    scope?: "PARAMETERS" | "CONNECTIVITY";
    status?: "OK" | "ERROR" | "UNSUPPORTED";
}
export interface Scheduler {
    type?: "timer" | "cron";
    expression?: string;
}
export interface Step {
    stepKind?: "endpoint" | "connector" | "expressionFilter" | "ruleFilter" | "extension" | "mapper" | "choice" | "split" | "aggregate" | "log" | "headers" | "template";
    connection?: Connection;
    name?: string;
    extension?: Extension;
    action?: Action;
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
    entrypoint?: string;
    resource?: string;
    kind?: "STEP" | "BEAN" | "ENDPOINT";
    propertyDefinitionSteps?: ActionDescriptorStep[];
    outputDataShape?: DataShape;
    inputDataShape?: DataShape;
}
export interface StreamingOutput {
}
export interface TargetStateRequest {
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
}
export interface User {
    firstName?: string;
    integrations?: Integration[];
    lastName?: string;
    roleId?: string;
    organizationId?: string;
    fullName?: string;
    name?: string;
    username?: string;
    id?: string;
}
export interface Validator {
}
export interface VerifierError {
    parameters?: string[];
    attributes?: {
        [name: string]: {
        };
    };
    description?: string;
    code?: string;
}
export interface Violation {
}
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
