import { Action } from '@ngrx/store';

import {
  IntegrationImportEditState,
  IntegrationImportUploadState
} from './integration-import.models';

export const UPLOAD_INTEGRATION = '[Integrations] Upload imported integration';
export const UPLOAD_INTEGRATION_COMPLETE = '[Integrations] Uploaded imported integration';
export const UPLOAD_INTEGRATION_FAIL = '[Integrations] Upload integration failed';

export const EDIT_INTEGRATION = '[Integrations] Edit imported integration';
export const EDIT_INTEGRATION_COMPLETE = '[Integrations] Edited imported integration';
export const EDIT_INTEGRATION_FAIL = '[Integrations] Edit integration failed';

export class IntegrationImportActions {}

export class IntegrationImportUpload implements Action {
  readonly type = UPLOAD_INTEGRATION;

  constructor(public payload: IntegrationImportUploadState) { }
}

export class IntegrationImportEdit implements Action {
  readonly type = EDIT_INTEGRATION;

  constructor(public payload: IntegrationImportEditState) { }
}



