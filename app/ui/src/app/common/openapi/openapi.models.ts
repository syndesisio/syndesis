import { StringMap } from '@syndesis/ui/platform';

export interface OpenApiValidationActionsSummary {
  totalActions: number;
  actionCountByTags?: StringMap<number>;
}

export interface OpenApiValidationWarning {
  error: string;
  message: string;
}

export interface OpenApiValidationError {
  error: string;
  message: string;
  property: string;
}

export type OpenApiValidationWarnings = OpenApiValidationWarning[];
export type OpenApiValidationErrors = OpenApiValidationError[];

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
  spec?: OpenApiUploaderValue;
  valid: boolean;
}
