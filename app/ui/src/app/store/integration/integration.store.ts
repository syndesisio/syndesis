import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Integrations, Integration, TypeFactory } from '@syndesis/ui/model';

import { IntegrationService } from './integration.service';
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

  public activate(integration: Integration): Observable<Integration> {
    integration.desiredStatus = 'Active';
    return this.update(integration);
  }

  public deactivate(integration: Integration): Observable<Integration> {
    integration.desiredStatus = 'Inactive';
    return this.update(integration);
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
