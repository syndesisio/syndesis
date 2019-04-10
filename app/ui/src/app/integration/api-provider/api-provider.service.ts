// import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { OpenApiUploaderValue } from '@syndesis/ui/common';
import { Observable } from 'rxjs';

import { ApiHttpService, Integration, integrationEndpoints } from '@syndesis/ui/platform';

import { apiProviderEndpoints } from '@syndesis/ui/integration/api-provider/api-provider.api';
import { ApiProviderValidationResponse } from '@syndesis/ui/integration/api-provider/api-provider.models';

@Injectable()
export class ApiProviderService {
  constructor(private apiHttpService: ApiHttpService) {}

  validateOpenApiSpecification(
    uploadSpec: OpenApiUploaderValue
  ): Observable<ApiProviderValidationResponse> {
    const apiHttpService = this.apiHttpService.setEndpointUrl(
      apiProviderEndpoints.validateOpenApiSpecification
    );
    return apiHttpService.upload<ApiProviderValidationResponse>(
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
