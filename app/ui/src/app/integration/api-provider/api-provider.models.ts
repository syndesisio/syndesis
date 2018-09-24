import {
  OpenApiValidationResponse,
  OpenApiUploadSpecification,
  OpenApiValidationError
} from '@syndesis/ui/common';

export enum ApiProviderWizardSteps {
  UploadSpecification = 1,
  ReviewApiProvider = 2,
  EditSpecification = 3,
  SubmitRequest = 4
}

export interface ApiProviderState {
  loading: boolean;
  loaded: boolean;
  hasErrors: boolean;
  wizardStep: ApiProviderWizardSteps;
  uploadSpecification?: OpenApiUploadSpecification;
  validationResponse?: OpenApiValidationResponse;
  validationErrors?: OpenApiValidationError;
}
