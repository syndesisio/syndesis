import { filter } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

import { IntegrationService } from '@syndesis/ui/store/integration/integration.service';

import {
  createIntegration,
  createStep,
  Integration,
  Integrations,
  Flow,
  key
} from '@syndesis/ui/platform';

import {
  AbstractStore,
  EventsService,
  ChangeEvent
} from '@syndesis/ui/store/entity';

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
    return 'integration';
  }

  setChangeEventsFilter(changeEvents: Subject<ChangeEvent>) {
    return changeEvents.pipe(
      filter(event => event.kind.startsWith('integration'))
    );
  }

  newInstance(): Integration {
    const integration = createIntegration();
    const start = createStep();
    const end = createStep();
    start.stepKind = end.stepKind = 'endpoint';
    integration.flows = [
      {
        id: key(),
        steps: [start, end]
      } as Flow
    ];
    return integration;
  }

  specification(integrationId: string): Observable<string> {
    return this.service.fetchSpecification(integrationId);
  }

  updateSpecification(integrationId: string, specification: string): Observable<void> {
    return this.service.updateSpecification(integrationId, specification);
  }
}
