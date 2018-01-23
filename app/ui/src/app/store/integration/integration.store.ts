import { Injectable } from '@angular/core';
import { IntegrationService } from './integration.service';
import { Integrations, Integration, TypeFactory } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';

@Injectable()
export class IntegrationStore extends AbstractStore<
  Integration,
  Integrations,
  IntegrationService
> {
  constructor(
    integrationService: IntegrationService,
    eventService: EventsService
  ) {
    super(integrationService, eventService, [], <Integration>{});
  }

  protected get kind() {
    return 'Integration';
  }

  public activate(i: Integration) {
    const _i = JSON.parse(JSON.stringify(i));
    _i.desiredStatus = 'Active';
    return this.update(_i);
  }

  public deactivate(i: Integration) {
    const _i = JSON.parse(JSON.stringify(i));
    _i.desiredStatus = 'Inactive';
    return this.update(_i);
  }

  newInstance(): Integration {
    const integration = TypeFactory.createIntegration();
    const start = TypeFactory.createStep();
    const end = TypeFactory.createStep();
    start.stepKind = end.stepKind = 'endpoint';
    integration.steps = [start, end];
    return integration;
  }
}
