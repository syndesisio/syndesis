export interface IResource {
  kind: string;
  id: string;
}

export interface IIntegration {
  board: IIntegrationBoard;
  createdAt: number;
  currentState: 'Published' | 'Unpublished' | 'Pending' | 'Error';
  deploymentVersion: number;
  deployments: any[];
  flows: IIntegrationFlow[];
  id: string;
  isDraft: boolean;
  name: string;
  resources: IResource[];
  tags: string[];
  targetState: string;
  updatedAt: number;
  url: string;
  version: number;
}

export interface IIntegrationFlow {
  id: string;
  name: string;
  steps: IIntegrationFlowStep[];
}

export interface IIntegrationFlowStep {
  action: IAction;
  configuredProperty: { [key: string]: string };
  connection: IConnection;
  id: string;
  metadata: { [key: string]: string };
  stepKind: string;
}

export interface IIntegrationBoard {
  createdAt: number;
  errors: number;
  id: string;
  metadata: { [id: string]: number };
  notices: number;
  targetResourceId: string;
  updatedAt: number;
  warnings: number;
}

export interface IIntegrationsMetrics {
  errors: number;
  lastProcessed: number;
  messages: number;
  metricsProvider: string;
  start: number;
  topIntegrations: IIntegrationsMetricsTopIntegration;
}

export interface IIntegrationsMetricsTopIntegration {
  [id: string]: number;
}

export interface IMonitoredIntegration {
  integration: IIntegration;
  monitoring?: IIntegrationMonitoring;
}

export interface IIntegrationMonitoring {
  deploymentVersion: number;
  detailedState: {
    value: string;
    currentStep: number;
    totalSteps: number;
  };
  value: string;
  id: string;
  integrationId: string;
  linkType: string;
  namespace: string;
  podName: string;
}

export interface IConnection {
  board?: {
    createdAt: number;
    updatedAt: number;
  };
  configuredProperties?: { [key: string]: any };
  connector: IConnector;
  connectorId: string;
  description: string;
  icon: string;
  id: string;
  isDerived: boolean;
  name: string;
  tags: string[];
  uses: number;
}

export interface IConnector {
  actions: IAction[];
  connectorCustomizers: string[];
  dependencies: IConnectorDependency[];
  description: string;
  icon: string;
  id: string;
  name: string;
  properties: any; // TODO
  tags: string[];
  version: number;
}

export interface IConnectorDependency {
  id: string;
  type: string;
}

export interface IAction {
  actionType: string;
  description: string;
  descriptor: IActionDescriptor;
  id: string;
  name: string;
  pattern: string;
  tags: string[];
}

export interface IActionDescriptor {
  componentScheme: string;
  connectorCustomizers: string[];
  inputDataShape: {
    kind: string;
  };
  outputDataShape: {
    kind: string;
  };
  propertyDefinitionSteps: IPropertyDefinitionStep[];
}

export interface IPropertyDefinitionStep {
  description: string;
  name: string;
  properties: any; // TODO
}
