import {
  ConfigurationProperty,
  IntegrationMetricsSummary,
  IntegrationOverview,
} from './models';

// TODO remove when these values are advertised by the swagger
export interface IConfigurationProperty extends ConfigurationProperty {
  required?: boolean;
  secret?: boolean;
}

export interface IntegrationWithOverview {
  integration: IntegrationOverview;
  overview?: IntegrationMetricsSummary;
}

export interface IntegrationWithMonitoring {
  integration: IntegrationOverview;
  monitoring?: IntegrationMonitoring;
}

export interface IntegrationMonitoring {
  deploymentVersion: number;
  detailedState: {
    value: string;
    currentStep: number;
    totalSteps: number;
  };
  value: string;
  id: string;
  integrationId: string;
  linkType: 'LOGS' | 'EVENTS';
  namespace: string;
  podName: string;
}
