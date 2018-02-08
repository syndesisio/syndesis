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

/**
 * Step 1: Integration Upload
 * User selects one or more integration files and uploads them.
 */
export class IntegrationImportUpload implements Action {
  readonly type = UPLOAD_INTEGRATION;

  constructor(public payload: IntegrationImportUploadState) { }
}

/**
 * Step 2: Integration Edit
 * Once the integration has been uploaded, it is available for review.
 * Once the user has reviewed the payload, they can then press 'Done' to
 * proceed to the Edit Integration view, which will be populated.
 */
export class IntegrationImportEdit implements Action {
  readonly type = EDIT_INTEGRATION;

  constructor(public payload: IntegrationImportEditState) { }
}



