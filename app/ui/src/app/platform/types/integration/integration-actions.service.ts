import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Integration,
  DRAFT,
  PENDING,
  PUBLISHED,
  UNPUBLISHED,
  Step } from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationActionsService {
  abstract canActivate(integration: Integration): boolean;
  abstract canDeactivate(integration: Integration): boolean;
  abstract requestAction(action: string, integration: Integration);
  abstract getStart(integration: Integration): Step;
  abstract getFinish(integration: Integration): Step;
  abstract getModalTitle(): string;
  abstract getModalMessage(): string;
}
