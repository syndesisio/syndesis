// import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { OpenApiValidationResponse, OpenApiUploaderValue } from '@syndesis/ui/common';
import { Observable } from 'rxjs';

import { ApiHttpService, Integration, integrationEndpoints } from '@syndesis/ui/platform';

import { apiProviderEndpoints } from '@syndesis/ui/integration/api-provider/api-provider.api';

@Injectable()
export class ApiProviderService {
  constructor(private apiHttpService: ApiHttpService) {}

  validateOpenApiSpecification(
    uploadSpec: OpenApiUploaderValue
  ): Observable<OpenApiValidationResponse> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiProviderEndpoints.validateOpenApiSpecification
    );
    return apiHttpService.upload<OpenApiValidationResponse>(
      // @ts-ignore
      {
        specification: uploadSpec
      },
      {}
    );
  }

  getIntegrationFromSpecification(
    specification: string
  ): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiProviderEndpoints.getIntegrationFromSpecification
    );

    return apiHttpService.upload(
      // @ts-ignore
      {
        specification
      }
    );
  }

  createIntegration(
    integration: Integration
  ): Observable<any> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      integrationEndpoints.integrations
    );

    return apiHttpService.post(integration);
  }
}
