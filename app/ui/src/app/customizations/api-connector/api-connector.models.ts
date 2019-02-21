import {
  StringMap,
  BaseReducerModel,
  BaseRequestModel,
  ActionReducerError
} from '@syndesis/ui/platform';
import {
  OpenApiUploadSpecification,
  OpenApiValidationActionsSummary,
  OpenApiValidationError,
  OpenApiValidationErrors,
  OpenApiValidationWarnings
} from '@syndesis/ui/common';

export enum ApiConnectorWizardStep {
  UploadSwagger = 1,
  ReviewApiConnector = 2,
  UpdateAuthSettings = 3,
  SubmitRequest = 4
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
  errors?: Array<OpenApiValidationError>;
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
  wizardStep: ApiConnectorWizardStep;
  showApiEditor: boolean;
  uploadSpecification: OpenApiUploadSpecification;
  validationResponse?: ApiConnectorValidationResponse;
  validationErrors?: OpenApiValidationError[];
  creationError?: ActionReducerError;
  specificationForEditor: string;
}

export interface ApiConnectorValidationResponse {
  actionsSummary: OpenApiValidationActionsSummary;
  name: string;
  description: string;
  warnings?: OpenApiValidationWarnings;
  errors?: OpenApiValidationErrors;
  configuredProperties?: {
    specification: string;
  };
}
