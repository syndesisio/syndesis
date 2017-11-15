import { Injectable } from '@angular/core';
import { TemplateService } from './template.service';
import { IntegrationTemplate } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class TemplateStore extends AbstractStore<
  IntegrationTemplate,
  Array<IntegrationTemplate>,
  TemplateService
> {
  constructor(
    integrationService: TemplateService,
    eventService: EventsService
  ) {
    super(integrationService, eventService, [], <IntegrationTemplate>{});
  }

  protected get kind() {
    return 'Template';
  }
}
