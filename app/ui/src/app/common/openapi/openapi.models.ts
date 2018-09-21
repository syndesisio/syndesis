import {
  StringMap,
  // BaseReducerModel,
  // BaseRequestModel
} from '@syndesis/ui/platform';

export interface OpenApiValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface RequestProperties {
  specification?: string;
  host?: string;
  basePath?: string;
}

export interface OpenApiData {
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

export interface OpenApiUploadSpecification {
  url?: string;
  file?: File;
}
//
// export type OpenApis = Array<OpenApiData>;
//
// export interface CustomConnectorRequest
//   extends CustomOpenApiRequest,
//     OpenApiData {
//   specificationFile?: File;
//   iconFile?: File;
// }
//
// export interface OpenApiState extends BaseReducerModel {
//   list: OpenApis;
//   createRequest: CustomConnectorRequest;
//   deleted?: OpenApiData;
// }
