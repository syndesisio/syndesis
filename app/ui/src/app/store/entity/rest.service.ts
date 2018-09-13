import { map } from 'rxjs/operators';
import { Observable, identity } from 'rxjs';

import { BaseEntity, ApiHttpService } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

export abstract class RESTService<T extends BaseEntity, L extends Array<T>> {
  perPage: number;
  protected constructor(
    public apiHttpService: ApiHttpService,
    public endpoint: string,
    public kind: string,
    configService: ConfigService,
    protected transformFn: (T) => T = identity
  ) {
    this.perPage = configService.getSettings().perPage || 50;
  }

  get(id: string): Observable<T> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment(id))
      .get()
      .pipe(
        map(this.transformFn)
      );
  }

  list(): Observable<L> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment() + '?per_page=' + this.perPage)
      .get()
      .pipe(
        map((response: any) => {
          const items: L =  Array.isArray(response) ? response : response.items || ([] as L);
          return items.map(this.transformFn) as L;
        }),
      );
  }

  create(obj: T): Observable<T> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment())
      .post(obj)
      .pipe(
        map(this.transformFn)
      );
  }

  update(obj: T): Observable<T> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment(obj.id))
      .put(obj)
      .pipe(
        map((response: any) => (response !== null ? response : [])),
      );
  }

  delete(obj: T): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment(obj.id))
      .delete();
  }

  patch(id: string, attributes: any): Observable<any> {
    return this.apiHttpService
      .setEndpointUrl(this.getEndpointSegment(id))
      .patch(attributes);
  }

  private getEndpointSegment(id?: string): string {
    return id ? `/${this.endpoint}/${id}` : `/${this.endpoint}`;
  }
}
