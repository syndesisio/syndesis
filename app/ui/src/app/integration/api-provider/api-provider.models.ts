import {
  StringMap,
  BaseReducerModel,
  BaseRequestModel
} from '@syndesis/ui/platform';

export interface ApiProviderValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface RequestProperties extends CustomApiProviderAuthSettings {
  specification?: string;
}

export interface ApiProviderData {
  actionsSummary?: {
    actionCountByTags: StringMap<number>;
    totalActions: number;
  };
  name?: string;
  description?: string;
  warnings?: Array<{ key: string; longdesc: string }>;
  errors?: Array<ApiProviderValidationError>;
  configuredProperties?: RequestProperties;
}
