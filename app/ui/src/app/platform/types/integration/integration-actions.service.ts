import { Injectable } from '@angular/core';

import {
  Integration,
  IntegrationOverview,
  IntegrationDeployment,
  DeploymentOverview,
  Step
} from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationActionsService {
  abstract canActivate(integration: Integration): boolean;
  abstract canDeactivate(integration: Integration): boolean;
  abstract canEdit(integration: Integration): boolean;
  abstract requestAction(
    action: string,
    integration: Integration | IntegrationOverview,
    deployment?: IntegrationDeployment | DeploymentOverview
  );
  abstract getStart(integration: Integration): Step;
  abstract getFinish(integration: Integration): Step;
  abstract getModalTitle(): string;
  abstract getModalMessage(): string;
  abstract getModalType(): string;
  abstract getModalPrimaryText(): string;
}
