import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Integration } from '@syndesis/ui/platform';
import { IntegrationImportState } from './integration-import.models';

@Injectable()
export abstract class IntegrationImportService {
  /**
   * Expects a new IntegrationImportState object and submits it through a POST request to the REST API
   * @param {Integration} integrationImport The Integration file to be uploaded
   * @returns {Observable<any>} Whatever the API returns for this - TODO: Properly model this
   */
  abstract uploadIntegration(integrationImport: IntegrationImportState): Observable<any>;
}
