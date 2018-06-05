import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiHttpService } from '@syndesis/ui/platform';
import { IntegrationImportState } from './integration-import.models';

import { integrationImportEndpoints } from './integration-import.api';

@Injectable()
export class IntegrationImportService {
  constructor(private apiHttpService: ApiHttpService) {}

  uploadIntegration(
    integrationImport: IntegrationImportState
  ): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      integrationImportEndpoints.uploadIntegration
    );
    const [file] = [integrationImport, integrationImport.file];

    return apiHttpService.post(file);
  }
}
