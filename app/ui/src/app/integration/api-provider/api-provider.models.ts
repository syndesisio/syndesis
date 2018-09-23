import {
  StringMap,
} from '@syndesis/ui/platform';
import { OpenApiUploadSpecification } from '@syndesis/ui/common';

export interface ApiProviderValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface RequestProperties {
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
  configuredProperties?: RequestProperties;
}

export interface ApiProviderState {
  loading: boolean;
  loaded: boolean;
  hasErrors: boolean;
  errors?: Array<ApiProviderValidationError>;
  uploadSpecification?: OpenApiUploadSpecification;
  createRequest: ApiProviderData;
}
