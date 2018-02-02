import { Injectable } from '@angular/core';

import { Integration, Step } from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationActionsService {
  abstract canEdit(integration: Integration): boolean;
  abstract canActivate(integration: Integration): boolean;
  abstract canDeactivate(integration: Integration): boolean;
  abstract canDelete(integration: Integration): boolean;
  abstract requestAction(action: string, integration: Integration);
  abstract getStart(integration: Integration): Step;
  abstract getFinish(integration: Integration): Step;
  abstract getModalTitle(): string;
  abstract getModalMessage(): string;
}
