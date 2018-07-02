import {
  StringMap,
  BaseReducerModel,
  BaseRequestModel,
  BaseEntity
} from '@syndesis/ui/platform';

export interface ApiConnectorValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface RequestProperties extends CustomApiConnectorAuthSettings {
  specification?: string;
  host?: string;
  basePath?: string;
}

export interface ApiConnectorData {
  id?: string;
  actionsSummary?: {
    actionCountByTags: StringMap<number>;
    totalActions: number;
  };
  name?: string;
  description?: string;
  warnings?: Array<{ key: string; longdesc: string }>;
  errors?: Array<ApiConnectorValidationError>;
  icon?: string;
  configuredProperties?: RequestProperties;
  properties?: {
    basePath: {
      defaultValue: string;
    };
    host: {
      defaultValue: string;
    };
    authenticationType?: {
      defaultValue: string;
      enum: Array<{ label: string; value: string }>;
    };
    authorizationEndpoint?: {
      defaultValue: string;
    };
    tokenEndpoint?: {
      defaultValue: string;
    };
  };
}

export type ApiConnectors = Array<ApiConnectorData>;

export interface CustomApiConnectorRequest extends BaseRequestModel {
  connectorTemplateId: string;
}

export interface CustomApiConnectorAuthSettings {
  authenticationType?: string;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
}

export interface CustomConnectorRequest
  extends CustomApiConnectorRequest,
    ApiConnectorData {
  specificationFile?: File;
  iconFile?: File;
}

export interface ApiConnectorState extends BaseReducerModel {
  list: ApiConnectors;
  createRequest: CustomConnectorRequest;
  deleted?: ApiConnectorData;
}
