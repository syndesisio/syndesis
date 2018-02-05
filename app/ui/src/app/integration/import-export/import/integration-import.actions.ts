import { Action } from '@ngrx/store';
import { ActionReducerError } from '@syndesis/ui/platform';
import { IntegrationImportRequest, IntegrationUploadRequest } from './integration-import.models';
import { Integrations } from '@syndesis/ui/platform';

export class IntegrationImportActions {
  static IMPORT = '[Import Integration] Import integration(s) request';
  static IMPORT_COMPLETE = '[Import Integration] Import integration complete';
  static IMPORT_FAIL = '[Import Integration] Import integration failed';
  static UPLOAD = '[Import Integration] Upload integration for import request';
  static UPLOAD_CANCEL = '[Import Integration] Upload integration for import cancelled';
  static UPLOAD_COMPLETE = '[Import Integration] Upload integration for import complete';
  static UPLOAD_FAIL = '[Import Integration] Upload integration for import failed';

  static import(payload: IntegrationImportRequest): IntegrationImport {
    return new IntegrationImport(payload);
  }

  static importFail(payload: ActionReducerError): IntegrationImportFail {
    return new IntegrationImportFail(payload);
  }

  static importComplete(payload: any): IntegrationImportComplete {
    return new IntegrationImportComplete(payload);
  }

  static upload(payload: IntegrationUploadRequest): IntegrationUpload {
    return new IntegrationUpload(payload);
  }

  static uploadFail(payload: ActionReducerError): IntegrationUploadFail {
    return new IntegrationUploadFail(payload);
  }

  static uploadComplete(payload: any): IntegrationUploadComplete {
    return new IntegrationUploadComplete(payload);
  }

  static uploadCancel(): IntegrationUploadCancel {
    return new IntegrationUploadCancel();
  }
}

export class IntegrationImport implements Action {
  readonly type = IntegrationImportActions.IMPORT;

  constructor(public payload: IntegrationUploadRequest) { }
}

export class IntegrationImportComplete implements Action {
  readonly type = IntegrationImportActions.IMPORT_COMPLETE;

  constructor(public payload: IntegrationUploadRequest) { }
}

export class IntegrationImportFail implements Action {
  readonly type = IntegrationImportActions.IMPORT_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class IntegrationUpload implements Action {
  readonly type = IntegrationImportActions.UPLOAD;

  constructor(public payload: IntegrationUploadRequest) { }
}

export class IntegrationUploadComplete implements Action {
  readonly type = IntegrationImportActions.UPLOAD_COMPLETE;

  constructor(public payload: IntegrationUploadRequest) { }
}

export class IntegrationUploadFail implements Action {
  readonly type = IntegrationImportActions.UPLOAD_FAIL;

  constructor(public payload: ActionReducerError) { }
}

export class IntegrationUploadCancel implements Action {
  readonly type = IntegrationImportActions.UPLOAD_CANCEL;
}
