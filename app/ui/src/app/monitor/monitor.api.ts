import { Endpoints } from '@syndesis/ui/platform';

// TODO: These endpoints are fictional and serve solely as a reference
export const monitorEndpoints = {
  integrations: '/metrics/integrations',
  integration: '/metrics/integrations/{0}',
  logs: '/logs/{0}',
  logDeltas: '/logs/deltas/{0}?from={1}',
};
