import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RESTService } from '../entity';
import {
  ApiHttpService,
  Extension,
  Extensions,
  Integrations
} from '@syndesis/ui/platform';

@Injectable()
export class ExtensionService extends RESTService<Extension, Extensions> {
  constructor(public apiHttpService: ApiHttpService) {
    super(apiHttpService, 'extensions', 'extension');
  }

  public getUploadUrl(id?: string) {
    const url = this.apiHttpService.getEndpointUrl('/extensions');
    return id ? `${url}?updatedId=${id}` : url;
  }

  public importExtension(id: string): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(`/extensions/${id}/install`)
      .post({});
  }

  public loadIntegrations(id: string): Observable<Integrations> {
    return this.apiHttpService
      .setEndpointUrl(`/extensions/${id}/integrations`)
      .get();
  }

  public list(): Observable<Extensions> {
    return super.list().pipe(
      map(extensions => {
        return extensions.filter(extension => extension.status !== 'Deleted');
      })
    );
  }
}
