import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService } from '@syndesis/ui/platform';
import { Integration } from '@syndesis/ui/integration';

import { monitorEndpoints } from './monitor.api';

@Injectable()
export class MonitorService {
  constructor(private apiHttpService: ApiHttpService) { }

  getIntegrationMetrics(): Observable<any> { // TODO: Assign proper <T> typing
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.integrations).get();
  }

  getIntegration(integration: Integration): Observable<any> { // TODO: Assign proper <T> typing
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.integration, integration.id).get();
  }

  getLogs(integration: Integration): Observable<any> { // TODO: Assign proper <T> typing
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.logs, integration.id).get();
  }

  getLogDeltas(payload: { integration: Integration; timestamp: number; }): Observable<any> { // TODO: Assign proper <T> typing
    const { integration, timestamp } = payload;
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.logDeltas, integration.id, timestamp).get();
  }
}
