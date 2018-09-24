import { Action } from '@ngrx/store';

import { ActionReducerError } from '@syndesis/ui/platform';
import { OpenApiValidationResponse, OpenApiUploadSpecification } from '@syndesis/ui/common';

export class ApiProviderActions {
  static NEXT_STEP = '[API Provider] Go to the next step';
  static PREV_STEP = '[API Provider] Back to the previous step';
  static UPDATE_SPEC = '[API Provider] Update OpenApi specification source';
  static VALIDATE_SPEC = '[API Provider] OpenApi validation request';
  static VALIDATE_SPEC_COMPLETE = '[API Provider] OpenApi validation complete';
  static VALIDATE_SPEC_FAIL = '[API Provider] OpenApi validation failed';
  static CREATE_CANCEL = '[API Provider] Create API Provider integration cancelled';

  static nextStep(): ApiProviderNextStep {
    return new ApiProviderNextStep();
  }

  static previousStep(): ApiProviderPreviousStep {
    return new ApiProviderPreviousStep();
  }

  static updateSpecification(
    payload: OpenApiUploadSpecification
  ): ApiProviderUpdateSpecification {
    return new ApiProviderUpdateSpecification(payload);
  }

  static validateSwagger(): ApiProviderValidateSwagger {
    return new ApiProviderValidateSwagger();
  }

  static validateSwaggerComplete(
    payload: OpenApiValidationResponse
  ): ApiProviderValidateSwaggerComplete {
    return new ApiProviderValidateSwaggerComplete(payload);
  }

  static validateSwaggerFail(
    payload: ActionReducerError
  ): ApiProviderValidateSwaggerFail {
    return new ApiProviderValidateSwaggerFail(payload);
  }

  static createCancel(): ApiProviderCreateCancel {
    return new ApiProviderCreateCancel();
  }
}

export class ApiProviderNextStep implements Action {
  readonly type = ApiProviderActions.NEXT_STEP;
}

export class ApiProviderPreviousStep implements Action {
  readonly type = ApiProviderActions.PREV_STEP;
}

export class ApiProviderUpdateSpecification implements Action {
  readonly type = ApiProviderActions.UPDATE_SPEC;

  constructor(public payload: OpenApiUploadSpecification) {}
}

export class ApiProviderValidateSwagger implements Action {
  readonly type = ApiProviderActions.VALIDATE_SPEC;
}

export class ApiProviderValidateSwaggerComplete implements Action {
  readonly type = ApiProviderActions.VALIDATE_SPEC_COMPLETE;

  constructor(public payload: OpenApiValidationResponse) {}
}

export class ApiProviderValidateSwaggerFail implements Action {
  readonly type = ApiProviderActions.VALIDATE_SPEC_COMPLETE;

  constructor(public payload: ActionReducerError) {}
}

export class ApiProviderCreateCancel implements Action {
  readonly type = ApiProviderActions.CREATE_CANCEL;
}
