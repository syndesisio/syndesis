import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiHttpService } from '@syndesis/ui/platform';

@Injectable()
export class TestSupportService {
  constructor(public apiHttpClient: ApiHttpService) {}

  resetDB() {
    return this.apiHttpClient.setEndpointUrl('/test-support/reset-db').get();
  }

  snapshotDB(): Observable<Blob> {
    return this.apiHttpClient
      .setEndpointUrl('/test-support/snapshot-db')
      .get({ responseType: 'blob' });
  }

  restoreDB(data: any) {
    return this.apiHttpClient
      .setEndpointUrl('/test-support/restore-db')
      .post(data);
  }
}
