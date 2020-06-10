/**
 * Customizable properties in API Client Connector wizard
 */
export interface ICreateConnectorProps {
  addTimestamp?: boolean;
  addUsernameTokenCreated?: boolean;
  addUsernameTokenNonce?: boolean;
  authenticationType?: string;
  authorizationEndpoint?: string;
  basePath?: string;
  connectorTemplateId?: string;
  description?: string;
  host?: string;
  icon?: string;
  name?: string;
  password?: string;
  passwordType?: string;
  /**
   * portName & serviceName
   * are used for SOAP
   */
  portName?: string;
  serviceName?: string;
  specification?: string;
  tokenEndpoint?: string;
  username?: string;
}
