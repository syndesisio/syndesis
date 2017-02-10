import { Injectable } from '@angular/core';
import { IntegrationService } from './integration.service';
import { Integrations, Integration } from './integration.model';

import { AbstractStore } from '../entity/entity.store';

@Injectable()
export class IntegrationStore extends AbstractStore<Integration, Integrations, IntegrationService> {
  constructor(integrationService: IntegrationService) {
    super(integrationService, [], <Integration>{});
  }

  protected get kind() { return 'Integration'; }

  newInstance(): Integration {
    return <Integration> {
          id: undefined,
          createdBy: undefined,
          createdOn: undefined,
          description: undefined,
          icon: undefined,
          modifiedBy: undefined,
          modifiedOn: undefined,
          name: '',
          kind: 'integration',
          configuredProperties: <Map<string, string>>{},
          steps: [],
        };
  }

}
