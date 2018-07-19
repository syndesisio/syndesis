import { Action } from '@ngrx/store';

import { ActionReducerError } from '@syndesis/ui/platform';
import {
  ApiConnectors,
  CustomConnectorRequest,
  ApiConnectorData,
  CustomApiConnectorAuthSettings
} from '@syndesis/ui/customizations/api-connector/api-connector.models';

export class ApiConnectorActions {
  static FETCH = '[API Connectors] Fetch connectors request';
  static FETCH_COMPLETE = '[API Connectors] Fetch operation complete';
  static FETCH_FAIL = '[API Connectors] Fetch operation failed';
  static VALIDATE_SWAGGER = '[API Connectors] Swagger validation request';
  static VALIDATE_SWAGGER_COMPLETE = '[API Connectors] Swagger validation complete';
  static VALIDATE_SWAGGER_FAIL = '[API Connectors] Swagger validation failed';
  static UPDATE_AUTH_SETTINGS = '[API Connectors] Update Api Connector auth settings';
  static CREATE = '[API Connectors] Create custom connector request';
  static CREATE_COMPLETE = '[API Connectors] Create custom connector complete';
  static CREATE_FAIL = '[API Connectors] Custom connector creation failed';
  static CREATE_CANCEL = '[API Connectors] Create custom connector cancelled';
  static UPDATE = '[API Connectors] Custom connector update request';
  static UPDATE_COMPLETE = '[API Connectors] Custom connector update complete';
  static UPDATE_FAIL = '[API Connectors] Custom connector update failed';
  static DELETE = '[API Connectors] Delete custom connector';
  static DELETE_COMPLETE = '[API Connectors] Custom connector successfully deleted';
  static DELETE_FAIL = '[API Connectors] Delete custom connector failed';

  static fetch() {
    return new ApiConnectorFetch();
  }

  static fetchComplete(payload: ApiConnectors): ApiConnectorFetchComplete {
    return new ApiConnectorFetchComplete(payload);
  }

  static validateSwagger(
    payload: CustomConnectorRequest
  ): ApiConnectorValidateSwagger {
    return new ApiConnectorValidateSwagger(payload);
  }

  static validateSwaggerComplete(
    payload: ApiConnectorData
  ): ApiConnectorValidateSwaggerComplete {
    return new ApiConnectorValidateSwaggerComplete(payload);
  }

  static validateSwaggerFail(
    payload: ActionReducerError
  ): ApiConnectorValidateSwaggerFail {
    return new ApiConnectorValidateSwaggerFail(payload);
  }

  static updateAuthSettings(
    payload: CustomApiConnectorAuthSettings
  ): ApiConnectorUpdateAuthSettings {
    return new ApiConnectorUpdateAuthSettings(payload);
  }

  static create(payload: CustomConnectorRequest): ApiConnectorCreate {
    return new ApiConnectorCreate(payload);
  }

  static createFail(payload: ActionReducerError): ApiConnectorCreateFail {
    return new ApiConnectorCreateFail(payload);
  }

  static createComplete(payload: any): ApiConnectorCreateComplete {
    return new ApiConnectorCreateComplete(payload);
  }

  static update(payload: CustomConnectorRequest): ApiConnectorUpdate {
    return new ApiConnectorUpdate(payload);
  }

  static updateFail(payload: ActionReducerError): ApiConnectorUpdateFail {
    return new ApiConnectorUpdateFail(payload);
  }

  static updateComplete(): ApiConnectorUpdateComplete {
    return new ApiConnectorUpdateComplete();
  }

  static delete(payload: string): ApiConnectorDelete {
    return new ApiConnectorDelete(payload);
  }

  static deleteFail(payload: ActionReducerError): ApiConnectorDeleteFail {
    return new ApiConnectorDeleteFail(payload);
  }

  static deleteComplete(): ApiConnectorDeleteComplete {
    return new ApiConnectorDeleteComplete();
  }

  static createCancel(): ApiConnectorCreateCancel {
    return new ApiConnectorCreateCancel();
  }
}

export class ApiConnectorFetch implements Action {
  readonly type = ApiConnectorActions.FETCH;
}

export class ApiConnectorFetchComplete implements Action {
  readonly type = ApiConnectorActions.FETCH_COMPLETE;

  constructor(public payload: ApiConnectors) {}
}

export class ApiConnectorFetchFail implements Action {
  readonly type = ApiConnectorActions.FETCH_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class ApiConnectorValidateSwagger implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER;

  constructor(public payload: CustomConnectorRequest) {}
}

export class ApiConnectorValidateSwaggerComplete implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: ApiConnectorData) {}
}

export class ApiConnectorValidateSwaggerFail implements Action {
  readonly type = ApiConnectorActions.VALIDATE_SWAGGER_COMPLETE;

  constructor(public payload: ActionReducerError) {}
}

export class ApiConnectorUpdateAuthSettings implements Action {
  readonly type = ApiConnectorActions.UPDATE_AUTH_SETTINGS;

  constructor(public payload: CustomApiConnectorAuthSettings) {}
}

export class ApiConnectorCreate implements Action {
  readonly type = ApiConnectorActions.CREATE;

  constructor(public payload: CustomConnectorRequest) {}
}

export class ApiConnectorCreateComplete implements Action {
  readonly type = ApiConnectorActions.CREATE_COMPLETE;

  constructor(public payload: CustomConnectorRequest) {}
}

export class ApiConnectorCreateFail implements Action {
  readonly type = ApiConnectorActions.CREATE_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class ApiConnectorUpdate implements Action {
  readonly type = ApiConnectorActions.UPDATE;

  constructor(public payload: CustomConnectorRequest) {}
}

export class ApiConnectorUpdateComplete implements Action {
  readonly type = ApiConnectorActions.UPDATE_COMPLETE;
}

export class ApiConnectorUpdateFail implements Action {
  readonly type = ApiConnectorActions.UPDATE_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class ApiConnectorDelete implements Action {
  readonly type = ApiConnectorActions.DELETE;

  constructor(public payload: string) {}
}

export class ApiConnectorDeleteComplete implements Action {
  readonly type = ApiConnectorActions.DELETE_COMPLETE;
}

export class ApiConnectorDeleteFail implements Action {
  readonly type = ApiConnectorActions.DELETE_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class ApiConnectorCreateCancel implements Action {
  readonly type = ApiConnectorActions.CREATE_CANCEL;
}
