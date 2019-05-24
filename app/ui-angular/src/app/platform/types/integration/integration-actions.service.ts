import { Injectable, TemplateRef } from '@angular/core';

import {
  Integration,
  IntegrationOverview,
  IntegrationDeployment,
  DeploymentOverview,
  Step,
} from '@syndesis/ui/platform';

@Injectable()
export abstract class IntegrationActionsService {
  abstract canPublish(integration: Integration): boolean;
  abstract canActivate(integration: Integration): boolean;
  abstract canDeactivate(integration: Integration): boolean;
  abstract canEdit(integration: Integration): boolean;
  abstract requestAction(
    action: string,
    integration: Integration | IntegrationOverview,
    deployment?: IntegrationDeployment | DeploymentOverview,
    templates?: Map<string, TemplateRef<any>>
  );
  abstract getStart(integration: Integration): Step;
  abstract getFinish(integration: Integration): Step;
  abstract getModalTitle(): string;
  abstract getModalMessage(): string;
  abstract getModalType(): string;
  abstract getModalPrimaryText(): string;
}
