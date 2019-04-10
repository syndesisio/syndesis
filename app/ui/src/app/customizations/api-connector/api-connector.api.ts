import { Endpoints } from '@syndesis/ui/platform';

export const apiConnectorEndpoints: Endpoints = {
  selectApiConnector: '/connectors/{id}',
  getApiConnectorList: '/connectors?query=connectorGroupId%3D{template}',
  validateCustomConnectorInfo: '/connectors/custom/info',
  submitCustomConnector: '/connectors/custom'
};
