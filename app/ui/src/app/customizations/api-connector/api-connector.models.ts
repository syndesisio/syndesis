import { StringMap, BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { BaseEntity } from '@syndesis/ui/model';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: ApiConnectorData;
}

export type ApiConnectors = Array<ApiConnectorData>;

export interface ApiConnectorProperties {
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

export interface ApiConnectorData {
  id: string;
  name: string;
  description: string;
  icon: string;
  fileIcon?: File;
  properties: ApiConnectorProperties;
  connectorProperties: any;
  host: string;
  baseUrl: string;
}

export interface ApiConnector extends BaseEntity {
  data: ApiConnectorData;
}

// XXX: The following models are designed to fit into the Redux container
//      and will eventually replace the ones above

export interface ApiConnectorValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface ApiConnectorValidation {
  actionsSummary?: {
    actionCountByTags: StringMap<number>;
    totalActions: number;
  };
  name?: string;
  description?: string;
  warnings?: Array<{ key: string; longdesc: string; }>;
  errors?: Array<ApiConnectorValidationError>;
  icon?: string;
  properties?: {
    basePath: {
      defaultValue: string;
    };
    host: {
      defaultValue: string;
    };
    authenticationType?: {
      defaultValue: string;
      enum: Array<{ label: string; value: string; }>;
    };
    authorizationEndpoint?: {
      defaultValue: string;
    };
  };
}

export interface CustomApiConnectorRequest {
  connectorTemplateId: string;
}

export interface CustomApiConnectorAuthSettings {
  authenticationType?: string;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
}

export interface RequestProperties extends CustomApiConnectorAuthSettings {
  specification?: string;
  host?: string;
  basePath?: string;
}

export interface CustomSwaggerConnectorRequest extends CustomApiConnectorRequest, ApiConnectorValidation {
  configuredProperties?: RequestProperties;
  file?: File;
}

export interface ApiConnectorState extends BaseReducerModel {
  list: ApiConnectors;
  createRequest: CustomSwaggerConnectorRequest;
}
