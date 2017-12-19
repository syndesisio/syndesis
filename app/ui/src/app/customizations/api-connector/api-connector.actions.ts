import { Action } from '@ngrx/store';

import { ActionReducerError } from '@syndesis/ui/platform';
import {
  ApiConnectors,
  CustomSwaggerConnectorRequest,
  ApiConnectorValidation,
  ApiConnectorData
} from './api-connector.models';

export class ApiConnectorActions {
  static FETCH                        = '[API Connectors] Fetch connectors request';
  static FETCH_COMPLETE               = '[API Connectors] Fetch operation complete';
  static FETCH_FAIL                   = '[API Connectors] Fetch operation failed';
  static VALIDATE_SWAGGER             = '[API Connectors] Swagger validation request';
  static VALIDATE_SWAGGER_COMPLETE    = '[API Connectors] Swagger validation complete';
  static CREATE                       = '[API Connectors] Create custom connector request';
  static CREATE_COMPLETE              = '[API Connectors] Create custom connector complete';
  static CREATE_CANCEL                = '[API Connectors] Create custom connector cancelled';

  static fetch() {
    return new ApiConnectorFetch();
  }

  static fetchComplete(payload: ApiConnectors) {
    return new ApiConnectorFetchComplete(payload);
  }

  static validateSwagger(payload: CustomSwaggerConnectorRequest) {
    return new ApiConnectorValidateSwagger(payload);
  }

  static validateSwaggerComplete(payload: ApiConnectorValidation) {
    return new ApiConnectorValidateSwaggerComplete(payload);
  }

  static create(payload: ApiConnectorData) {
    return new ApiConnectorCreate(payload);
  }

  static createComplete(payload: any) {
    return new ApiConnectorCreateComplete(payload);
  }

  static createCancel() {
    return new ApiConnectorCreateCancel();
  }
}

export class ApiConnectorFetch implements Action {
  readonly type = ApiConnectorActions.FETCH;
}

export class ApiConnectorFetchComplete implements Action {
  readonly type = ApiConnectorActions.FETCH_COMPLETE;

  constructor(public payload: ApiConnectors) { }
}

export class ApiConnectorFetchFail implements Action {
  readonly type = ApiConnectorActions.FETCH_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class ApiConnectorValidateSwagger implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER;

  constructor(public payload: CustomSwaggerConnectorRequest) { }  // TODO: Review payload type
}

export class ApiConnectorValidateSwaggerComplete implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: ApiConnectorValidation) { }  // TODO: Review payload type
}

export class ApiConnectorCreate implements Action {
  readonly type = ApiConnectorActions.CREATE;

  constructor(public payload: ApiConnectorData) { }  // TODO: Review payload type
}

export class ApiConnectorCreateComplete implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: any) { }  // TODO: Review payload type
}

export class ApiConnectorCreateCancel implements Action {
  readonly type = ApiConnectorActions.CREATE_CANCEL;
}
