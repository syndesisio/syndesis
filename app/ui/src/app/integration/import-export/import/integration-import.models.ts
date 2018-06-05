import { BaseReducerModel, BaseRequestModel } from '@syndesis/ui/platform';

export interface IntegrationImportValidationError {
  error?: string;
  message: string;
  property?: string;
}
export type IntegrationImports = Array<IntegrationImportState>;

export interface IntegrationImportRequest
  extends BaseRequestModel,
    IntegrationImportState {
  integrationImportTemplateId: string;
}

export interface IntegrationImportState extends BaseReducerModel {
  errors?: Array<IntegrationImportValidationError>;
  file?: File;
  importResults?: {
    integrations?: string[];
    connections?: string[];
    action?: string;
    kind?: string;
    id?: string;
  };
  list?: IntegrationImports;
  warnings?: Array<{ key: string; longdesc: string }>;
}

export interface FileError {
  level: string;
  message: string;
}

export interface IntegrationImportData {
  action?: string;
  kind?: string;
  id?: string;
}

export type IntegrationImportsData = Array<IntegrationImportData>;
