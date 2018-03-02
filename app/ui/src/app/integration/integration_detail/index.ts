import { Type } from '@angular/core';

import { INTEGRATION_ACTIVITY_DIRECTIVES } from './integration-activity';
import { IntegrationDescriptionComponent } from './integration-description.component';
import { IntegrationMetricsComponent } from './integration-metrics.component';

export * from './integration-detail.component';
export const INTEGRATION_DETAIL_DIRECTIVES: Type<any>[] = [
  ...INTEGRATION_ACTIVITY_DIRECTIVES,
  IntegrationDescriptionComponent,
  IntegrationMetricsComponent
];
