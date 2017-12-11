import { BaseEntity } from '../../model';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: ApiConnectorData;
}
export type ApiConnectors = Array<ApiConnector>;

export interface ApiConnectorData extends BaseEntity {
  id: string;
  name: string;
  description: string;
  icon: string;
  properties: ApiConnectorProperties;
  connectorProperties: {};
}

export interface ApiConnectorProperties extends BaseEntity {
  specification: {
    kind: string;
    displayName: string;
    required: boolean;
    type: string;
    javaType: string;
    description: string
  };
  specificationUrl: {
    kind: string;
    displayName: string;
    required: boolean;
    type: string;
    javaType: string;
    description: string;
  };
}
