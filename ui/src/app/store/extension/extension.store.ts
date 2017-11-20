import { Injectable } from '@angular/core';
import { ExtensionService } from './extension.service';
import { Extension, Extensions, TypeFactory } from '../../model';
import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class ExtensionStore extends AbstractStore<
  Extension,
  Extensions,
  ExtensionService
> {
  constructor(extensionService: ExtensionService, eventService: EventsService) {
    super(extensionService, eventService, [], TypeFactory.createExtension());
  }

  protected get kind() {
    return 'Extension';
  }
}
