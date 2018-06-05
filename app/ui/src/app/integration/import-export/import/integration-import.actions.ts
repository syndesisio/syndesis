import { ActionReducerError, Integration } from '@syndesis/ui/platform';
import { Action } from '@ngrx/store';

import { IntegrationImportState } from './integration-import.models';

export const UPLOAD_INTEGRATION = '[Integrations] Upload imported integration';
export const UPLOAD_INTEGRATION_COMPLETE =
  '[Integrations] Uploaded imported integration';
export const UPLOAD_INTEGRATION_FAIL =
  '[Integrations] Upload integration failed';

export const EDIT_INTEGRATION = '[Integrations] Edit imported integration';
export const EDIT_INTEGRATION_COMPLETE =
  '[Integrations] Edited imported integration';
export const EDIT_INTEGRATION_FAIL = '[Integrations] Edit integration failed';

export class IntegrationImportUpload implements Action {
  readonly type = UPLOAD_INTEGRATION;

  constructor(public entity: IntegrationImportState, public loading = false) {}
}

export class IntegratImportUploadComplete implements Action {
  readonly type = UPLOAD_INTEGRATION_COMPLETE;

  constructor(public payload: IntegrationImportState) {}
}

export class IntegratImportUploadFail implements Action {
  readonly type = UPLOAD_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) {}
}

export class IntegrationImportEdit implements Action {
  readonly type = EDIT_INTEGRATION;

  constructor(public entity: Integration, public loading = false) {}
}

export class IntegrationImportEditComplete implements Action {
  readonly type = EDIT_INTEGRATION_COMPLETE;
}

export class IntegrationImportEditFail implements Action {
  readonly type = EDIT_INTEGRATION_FAIL;

  constructor(public payload: ActionReducerError) {}
}
