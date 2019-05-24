import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { RESTService } from '@syndesis/ui/store/entity';
import {
  ApiHttpService,
  Extension,
  Extensions,
  Integrations
} from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ExtensionService extends RESTService<Extension, Extensions> {
  constructor(apiHttpService: ApiHttpService, configService: ConfigService) {
    super(apiHttpService, 'extensions', 'extension', configService);
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
