import { StringMap, BaseReducerModel, BaseRequestModel } from '@syndesis/ui/platform';
import { Integrations } from '@syndesis/ui/platform';

export interface IntegrationUploadValidationError {
  error?: string;
  message: string;
  property?: string;
}

export interface IntegrationImportValidationError {
  error?: string;
  message: string;
  property?: string;
}

/**
 * Used for both uploading and importing.
 */
export interface IntegrationRequestProperties extends IntegrationAuthSettings {
  specification?: string;
  host?: string;
  basePath?: string;
}

export interface IntegrationUploadData {
  id?: string;
  name?: string;
  description?: string;
  warnings?: Array<{ key: string; longdesc: string; }>;
  errors?: Array<IntegrationUploadValidationError>;
  configuredProperties?: IntegrationRequestProperties;
}

export type IntegrationUploadArray = Array<IntegrationUploadData>;

export interface IntegrationImportData {
  id?: string;
  name?: string;
  warnings?: Array<{ key: string; longdesc: string; }>;
  errors?: Array<IntegrationImportValidationError>;
  configuredProperties?: IntegrationRequestProperties;
}

export interface IntegrationUploadRequest extends BaseRequestModel {
  integrationTemplateId: string;
}

export interface IntegrationAuthSettings {
  authenticationType?: string;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
}

export interface IntegrationImportRequest extends IntegrationUploadRequest, IntegrationUploadData {
  specificationFile?: File;
  iconFile?: File;
}

export interface IntegrationImportState extends BaseReducerModel {
  list: Integrations;
  uploadRequest: IntegrationUploadRequest;
  importRequest: IntegrationImportRequest;
}
