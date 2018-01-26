import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService } from '@syndesis/ui/platform';
import { IntegrationImportData, IntegrationImportRequest } from './integration-import.models';

@Injectable()
export class IntegrationImportService {
  constructor(private apiHttpService: ApiHttpService) { }

  getImport(id: string): Observable<IntegrationImportData> {
    return this.apiHttpService
      .setEndpointUrl('selectImport', { id })
      .get<IntegrationImportData>();
  }

  validateImportInfo(importRequest: IntegrationImportRequest): Observable<IntegrationImportData> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('validateImportInfo');
    const { specificationFile, integrationTemplateId } = importRequest;
    if (specificationFile) {
      return apiHttpService.upload<IntegrationImportData>({
        specification: specificationFile
      }, {
        connectorSettings: { integrationTemplateId }
      });
    } else {
      return apiHttpService.post<IntegrationImportData>(importRequest);
    }
  }

  importIntegration(importRequest: IntegrationImportRequest): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl('submitImport');

    const [importIntegration, icon, specification] = [
      importRequest,
      importRequest.iconFile,
      importRequest.specificationFile
    ];

    if (specification || icon) {
      const payload = specification && icon ? { specification, icon } : { specification } || { icon };
      return apiHttpService.upload(payload, { importIntegration });
    } else {
      return apiHttpService.post(importIntegration);
    }
  }

  uploadIntegration(importRequest: IntegrationImportRequest): Observable<any> {
    return;
  }
}
