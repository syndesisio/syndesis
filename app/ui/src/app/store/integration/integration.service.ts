import { Injectable } from '@angular/core';

import {
  ApiHttpService,
  Integration,
  Integrations,
  IntegrationState
} from '@syndesis/ui/platform';
import { RESTService } from '../entity';

@Injectable()
export class IntegrationService extends RESTService<Integration, Integrations> {
  constructor(apiHttpService: ApiHttpService) {
    super(apiHttpService, 'integrations', 'integration');
  }
}
