import { Type } from '@angular/core';

import { IntegrationDescriptionComponent } from './integration-description.component';
import { IntegrationHistoryComponent } from './integration-history.component';
import { IntegrationMetricsComponent } from './integration-metrics.component';

export * from './integration-detail.component';
export const INTEGRATION_DETAIL_DIRECTIVES: Type<any>[] = [
  IntegrationDescriptionComponent,
  IntegrationHistoryComponent,
  IntegrationMetricsComponent
];
