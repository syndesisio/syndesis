export interface APISummary {
    warnings?: Violation[];
    errors?: Violation[];
    description?: string;
    icon?: string;
    actionsSummary?: ActionsSummary;
    name: string;
    properties?: {
        [name: string]: ConfigurationProperty;
    };
    configuredProperties?: {
        [name: string]: string;
    };
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
    type?: string;
    defaultValue?: string;
    displayName?: string;
    deprecated?: boolean;
    group?: string;
    label?: string;
    kind?: string;
    description?: string;
    enum?: PropertyValue[];
    generator?: string;
    placeholder?: string;
    javaType?: string;
    connectorValue?: string;
    relation?: PropertyRelation[];
    controlHint?: string;
    labelHint?: string;
    tags?: string[];
    order?: OptionalInt;
}
export interface Connection {
    id?: string;
    lastUpdated?: string; // date-time
    organizationId?: string;
    userId?: string;
    organization?: Organization;
    options?: {
        [name: string]: string;
    };
    description?: string;
    connector?: Connector;
    icon?: string;
    uses?: OptionalInt;
    derived?: boolean;
    connectorId?: string;
    createdDate?: string; // date-time
    tags?: string[];
    name: string;
    configuredProperties?: {
        [name: string]: string;
    };
    metadata?: {
        [name: string]: string;
    };
}
export interface ConnectionBulletinBoard {
    id?: string;
    targetResourceId?: string;
    warnings?: OptionalInt;
    errors?: OptionalInt;
    notices?: OptionalInt;
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
    lastUpdated?: string; // date-time
    organizationId?: string;
    userId?: string;
    organization?: Organization;
    options?: {
        [name: string]: string;
    };
    description?: string;
    connector?: Connector;
    icon?: string;
    uses?: OptionalInt;
    derived?: boolean;
    connectorId?: string;
    createdDate?: string; // date-time
    tags?: string[];
    name: string;
    configuredProperties?: {
        [name: string]: string;
    };
    metadata?: {
        [name: string]: string;
    };
}
export interface Connector {
    connectorGroup?: ConnectorGroup;
    description?: string;
    icon?: string;
    readonly uses?: OptionalInt;
    connectorFactory?: string;
    connectorCustomizers?: string[];
    connectorGroupId?: string;
    actionsSummary?: ActionsSummary;
    componentScheme?: string;
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
    connectorFactory?: string;
    connectorCustomizers?: string[];
    connectorId?: string;
    componentScheme?: string;
    camelConnectorGAV?: string;
    camelConnectorPrefix?: string;
    propertyDefinitionSteps?: ActionDescriptorStep[];
    inputDataShape?: DataShape;
    outputDataShape?: DataShape;
    configuredProperties?: {
        [name: string]: string;
    };
    split?: Split;
}
export interface ConnectorGroup {
    id?: string;
    name: string;
}
export interface ConnectorTemplate {
    connectorGroup?: ConnectorGroup;
    description?: string;
    icon?: string;
    connectorProperties?: {
        [name: string]: ConfigurationProperty;
    };
    camelConnectorGAV?: string;
    camelConnectorPrefix?: string;
    id?: string;
    name: string;
    properties?: {
        [name: string]: ConfigurationProperty;
    };
    configuredProperties?: {
        [name: string]: string;
    };
}
export interface DataManager {
}
export interface DataShape {
    name: string;
    type?: string;
    metadata?: {
        [name: string]: string;
    };
    kind?: "ANY" | "JAVA" | "JSON_SCHEMA" | "JSON_INSTANCE" | "XML_SCHEMA" | "XML_INSTANCE" | "NONE";
    description?: string;
    specification?: string;
    exemplar?: string /* byte */ [];
}
export interface Dependency {
    id?: string;
    type?: "MAVEN" | "EXTENSION" | "EXTENSION_TAG";
}
export interface Environment {
    id?: string;
    name: string;
}
export interface EventMessage {
    data?: {
    };
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
    status?: "Draft" | "Installed" | "Deleted";
    extensionType: "Steps" | "Connectors" | "Libraries";
    userId?: string;
    uses?: OptionalInt;
    createdDate?: string; // date-time
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
}
export interface FilterOptions {
    paths?: string[];
    ops?: Op[];
}
export interface Flow {
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
    description?: string;
    deleted?: boolean;
    flows?: Flow[];
    steps?: Step[];
    connections?: Connection[];
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
    warnings?: OptionalInt;
    errors?: OptionalInt;
    notices?: OptionalInt;
    id?: string;
    metadata?: {
        [name: string]: string;
    };
    messages?: LeveledMessage[];
    createdAt?: number; // int64
    updatedAt?: number; // int64
}
export interface IntegrationDeployment {
    integrationId?: string;
    spec?: Integration;
    userId?: string;
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    stepsDone?: {
        [name: string]: string;
    };
    statusMessage?: string;
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
    id?: string;
    version?: number; // int32
    createdAt?: number; // int64
    updatedAt?: number; // int64
}
export interface IntegrationDeploymentMetrics {
    start?: string; // date-time
    version?: string;
    errors?: number; // int64
    messages?: number; // int64
    lastProcessed?: string; // date-time
}
export interface IntegrationDeploymentOverview {
    userId?: string;
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    stepsDone?: {
        [name: string]: string;
    };
    statusMessage?: string;
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
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
    currentState?: "Published" | "Unpublished" | "Error" | "Pending";
    deployments?: IntegrationDeploymentOverview[];
    deploymentVersion?: number; // int32
    draft?: boolean;
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
    board?: IntegrationBulletinBoard;
    id?: string;
    description?: string;
    deleted?: boolean;
    flows?: Flow[];
    steps?: Step[];
    connections?: Connection[];
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
    code?: "SYNDESIS000" | "SYNDESIS001" | "SYNDESIS002" | "SYNDESIS003" | "SYNDESIS004" | "SYNDESIS005" | "SYNDESIS006" | "SYNDESIS007" | "SYNDESIS008" | "SYNDESIS009" | "SYNDESIS010" | "SYNDESIS011" | "SYNDESIS012";
    detail?: string;
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
    status?: "OK" | "ERROR" | "UNSUPPORTED";
    errors?: VerifierError[];
    scope?: "PARAMETERS" | "CONNECTIVITY";
}
export interface Scheduler {
    type?: "timer" | "cron";
    expression?: string;
}
export interface Split {
    language?: string;
    expression?: string;
}
export interface Step {
    name?: string;
    extension?: Extension;
    connection?: Connection;
    action?: Action;
    stepKind?: "endpoint" | "connector" | "expressionFilter" | "ruleFilter" | "extension" | "mapper" | "split" | "log" | "headers" | "template";
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
    kind?: "STEP" | "BEAN" | "ENDPOINT";
    entrypoint?: string;
    propertyDefinitionSteps?: ActionDescriptorStep[];
    inputDataShape?: DataShape;
    outputDataShape?: DataShape;
}
export interface StreamingOutput {
}
export interface TargetStateRequest {
    targetState?: "Published" | "Unpublished" | "Error" | "Pending";
}
export interface User {
    name?: string;
    organizationId?: string;
    username?: string;
    lastName?: string;
    roleId?: string;
    firstName?: string;
    fullName?: string;
    integrations?: Integration[];
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
    code?: string;
    description?: string;
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
