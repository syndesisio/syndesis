import {
  OpenApiUploadSpecification,
  OpenApiValidationActionsSummary,
  OpenApiValidationError,
  OpenApiValidationErrors,
  OpenApiValidationWarnings
} from '@syndesis/ui/common';
import { ActionReducerError } from '@syndesis/ui/platform';

export enum ApiProviderWizardSteps {
  UploadSpecification = 1,
  ReviewApiProvider = 2,
  EditSpecification = 3,
  IntegrationName = 4,
  SubmitRequest = 5
}

export type ApiProviderCreationErrors = Array<{ error: string; message: string }>;

export interface ApiProviderState {
  loading: boolean;
  loaded: boolean;
  hasErrors: boolean;
  wizardStep: ApiProviderWizardSteps;
  uploadSpecification: OpenApiUploadSpecification;
  validationResponse?: ApiProviderValidationResponse;
  validationErrors?: OpenApiValidationError;
  creationError?: ActionReducerError;
  integrationName?: string;
  integrationDescription?: string;
  specificationForEditor: string;
}

export interface ApiProviderValidationResponse {
  actionsSummary: OpenApiValidationActionsSummary;
  name: string;
  description: string;
  warnings?: OpenApiValidationWarnings;
  errors?: OpenApiValidationErrors;
  configuredProperties?: {
    specification: string;
  };
}
