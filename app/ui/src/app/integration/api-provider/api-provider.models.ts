import {
  OpenApiValidationResponse,
  OpenApiUploadSpecification,
  OpenApiValidationError
} from '@syndesis/ui/common';
import { ActionReducerError } from '@syndesis/ui/platform';

export enum ApiProviderWizardSteps {
  UploadSpecification = 1,
  ReviewApiProvider = 2,
  EditSpecification = 3,
  SubmitRequest = 4
}

export type ApiProviderCreationErrors = Array<{ error: string; message: string }>;

export interface ApiProviderState {
  loading: boolean;
  loaded: boolean;
  hasErrors: boolean;
  wizardStep: ApiProviderWizardSteps;
  uploadSpecification: OpenApiUploadSpecification;
  validationResponse?: OpenApiValidationResponse;
  validationErrors?: OpenApiValidationError;
  creationError?: ActionReducerError;
  specificationForEditor: string;
}
