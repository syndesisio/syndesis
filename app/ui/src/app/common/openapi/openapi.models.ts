import { StringMap } from '@syndesis/ui/platform';

export interface OpenApiValidationResponse {
  actionsSummary: {
    totalActions: number;
    actionCountByTags?: StringMap<number>;
  };
  name: string;
  description: string;
  warnings?: Array<{ key: string; longdesc: string }>;
  errors?: Array<{ key: string; longdesc: string }>;
  configuredProperties?: {
    specification: string;
  };
}

export interface OpenApiValidationErrorMessage {
  error?: string;
  message: string;
}

export interface OpenApiValidationError {
  errors: OpenApiValidationErrorMessage[];
}

export type OpenApiUploaderValue = File | string;

export enum OpenApiUploaderValueType {
  File = 'file',
  Url = 'url',
  Spec = 'spec'
}

export interface OpenApiUploadSpecification {
  type: OpenApiUploaderValueType;
  spec: OpenApiUploaderValue;
  valid: boolean;
}
