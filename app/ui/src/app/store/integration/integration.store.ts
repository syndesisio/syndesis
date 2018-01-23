import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { TypeFactory } from '@syndesis/ui/model';
import { IntegrationService } from './integration.service';

// pointing this at the integration barrel caused runtime issues
import { createIntegration, createStep, Integration, Integrations } from '@syndesis/ui/integration/integration.model';

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
    const integration = createIntegration();
    const start = createStep();
    const end = createStep();
    start.stepKind = end.stepKind = 'endpoint';
    integration.steps = [start, end];
    return integration;
  }
}
