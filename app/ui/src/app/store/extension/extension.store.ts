import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { ExtensionService } from './extension.service';
import { Extension, Extensions, Integrations } from '@syndesis/ui/platform';
import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class ExtensionStore extends AbstractStore<
  Extension,
  Extensions,
  ExtensionService
> {
  constructor(extensionService: ExtensionService, eventService: EventsService) {
    super(extensionService, eventService, [], {} as Extension);
  }

  protected get kind() {
    return 'Extension';
  }

  public getUploadUrl(id?: string): string {
    return this.service.getUploadUrl(id);
  }

  public importExtension(id: string): Observable<Response> {
    return this.service.importExtension(id);
  }

  public loadIntegrations(id: string): Observable<Integrations> {
    return this.service.loadIntegrations(id).map( resp => <Integrations> resp.json() );
  }

}
