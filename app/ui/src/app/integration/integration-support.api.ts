import { Endpoints } from '@syndesis/ui/platform';

export const integrationSupportEndpoints: Endpoints = {
  // TODO does this belong here really
  metadata: '/connections/{connectionId}/actions/{actionId}',
  // TODO should this go into the integration service
  filterOptions: '/integrations/filters/options',
  history: '/integrations/{id}/history',
  deployments: '/integrations/{id}/deployments',
  deployment: '/integrations/{id}/deployments/{version}',
  pom: '/integration-support/generate/pom.xml',
  export: '/integration-support/export.zip',
  import: '/integration-support/import',
  activityFeature: '/activity/feature',
  activity: '/activity/integrations/{integrationId}',
  supportData: '/support/downloadSupportZip',
  // TODO this path should be driven by config.json also does this belong here
  javaInspection: '/../../mapper/v1/java-inspections/{connectorId}/{type}.json',
};
