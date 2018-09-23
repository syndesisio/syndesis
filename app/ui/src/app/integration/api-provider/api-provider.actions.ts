import { Action } from '@ngrx/store';

import { ActionReducerError } from '@syndesis/ui/platform';
import { OpenApiUploadSpecification } from '@syndesis/ui/common';
import { ApiProviderData } from '@syndesis/ui/integration/api-provider/api-provider.models';

export class ApiProviderActions {
  static VALIDATE_SWAGGER = '[API Provider] OpenApi validation request';
  static VALIDATE_SWAGGER_COMPLETE = '[API Provider] OpenApi validation complete';
  static VALIDATE_SWAGGER_FAIL = '[API Provider] OpenApi validation failed';
  static CREATE_CANCEL = '[API Provider] Create API Provider integration cancelled';

  static validateSwagger(
    payload: OpenApiUploadSpecification
  ): ApiProviderValidateSwagger {
    return new ApiProviderValidateSwagger(payload);
  }

  static validateSwaggerComplete(
    payload: ApiProviderData
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

export class ApiProviderValidateSwagger implements Action {
  readonly type = ApiProviderActions.VALIDATE_SWAGGER;

  constructor(public payload: OpenApiUploadSpecification) {}
}

export class ApiProviderValidateSwaggerComplete implements Action {
  readonly type = ApiProviderActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: ApiProviderData) {}
}

export class ApiProviderValidateSwaggerFail implements Action {
  readonly type = ApiProviderActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: ActionReducerError) {}
}

export class ApiProviderCreateCancel implements Action {
  readonly type = ApiProviderActions.CREATE_CANCEL;
}
