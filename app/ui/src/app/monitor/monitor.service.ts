import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ApiHttpService } from '@syndesis/ui/platform';
import { Integration } from '@syndesis/ui/integration';

import { monitorEndpoints } from './monitor.api';

@Injectable()
export class MonitorService {
  constructor(private apiHttpService: ApiHttpService) { }

  getMetrics(integration: Integration): Observable<any> { // TBD: Assign proper <T> typing
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.metrics, integration.id).get();
  }

  getLogs(integration: Integration): Observable<any> { // TBD: Assign proper <T> typing
    return this.apiHttpService.setEndpointUrl(monitorEndpoints.logs, integration.id).get();
  }
}
