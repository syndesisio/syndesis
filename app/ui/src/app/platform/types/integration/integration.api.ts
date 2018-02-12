import { Endpoints } from '@syndesis/ui/platform';

export const integrationEndpoints: Endpoints = {
  integrations: '/integrations',
  integration: '/integrations/{id}',
  integrationMetrics: '/metrics/integrations',
  integrationMetricsById: '/metrics/integrations/{id}',
};
