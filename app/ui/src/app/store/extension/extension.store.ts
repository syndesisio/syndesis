import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Extension, Extensions, Integrations } from '@syndesis/ui/platform';
import { AbstractStore, EventsService } from '../entity';
import { ExtensionService } from './extension.service';

@Injectable()
export class ExtensionStore extends AbstractStore<
  Extension,
  Extensions,
  ExtensionService
> {
  constructor(extensionService: ExtensionService, eventService: EventsService) {
    super(extensionService, eventService, [], {} as Extension);
  }

  protected get kind(): string {
    return 'Extension';
  }

  public getUploadUrl(id?: string): string {
    return this.service.getUploadUrl(id);
  }

  public importExtension(id: string): Observable<Response> {
    return this.service.importExtension(id);
  }

  public loadIntegrations(id: string): Observable<Integrations> {
    return this.service.loadIntegrations(id);
  }
}
