import { StringMap, BaseReducerModel, PlatformStore } from '@syndesis/ui/platform';
import { BaseEntity } from '@syndesis/ui/model';

export interface ApiConnector extends BaseEntity {
  kind: string;
  data: ApiConnectorData;
}

export interface ApiConnectorData extends ApiConnectorValidation {
  id: string;
  name: string;
  description: string;
  icon: string;
  fileIcon?: File;
  properties: ApiConnectorProperties;
  connectorProperties: any;
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

export interface ApiConnectorData extends ApiConnectorValidation, CustomSwaggerConnectorRequest {
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
  error: string;
  message: string;
  property: string;
}

export interface ApiConnectorValidation {
  validationDetails?: {
    actionsSummary?: {
      actionCountByTags: StringMap<number>;
      totalActions: number;
    };
    warnings: Array<{ key: string; longdesc: string; }>;
    errors: Array<{ key: string; longdesc: string; }>;
  };
  errors?: Array<ApiConnectorValidationError>; // Rethink duplication of error properties
}

export interface CustomApiConnectorRequest {
  connectorTemplateId: string;
  name?: string;
  description?: string;
  icon?: string;
}

export interface CustomSwaggerConnectorRequest
  extends CustomApiConnectorRequest, ApiConnectorValidation {
  configuredProperties?: {
    specification?: string;
    host?: string;
    basePath?: string;
    authentication?: any;
  };
  file?: File;
}

export interface ApiConnectorState extends BaseReducerModel {
  list: ApiConnectors;
  createRequest: CustomSwaggerConnectorRequest;
}
