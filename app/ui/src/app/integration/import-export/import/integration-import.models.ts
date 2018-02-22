import {
  BaseReducerModel,
  BaseRequestModel
} from '@syndesis/ui/platform';

export interface IntegrationImportValidationError {
  error?: string;
  message: string;
  property?: string;
}
export type IntegrationImports = Array<IntegrationImportState>;

export interface IntegrationImportRequest extends BaseRequestModel, IntegrationImportState {
  integrationImportTemplateId: string;
}

export interface IntegrationImportState extends BaseReducerModel {
  createRequest?: IntegrationImportRequest;
  errors?: Array<IntegrationImportValidationError>;
  file?: File;
  importResults?: {
    integrations?: string[];
    connections?: string[];
  };
  list?: IntegrationImports;
  warnings?: Array<{ key: string; longdesc: string; }>;
}
