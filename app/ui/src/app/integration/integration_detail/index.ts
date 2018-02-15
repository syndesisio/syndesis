import { Type } from '@angular/core';

export * from './integration-detail.component';
import { IntegrationDescriptionComponent } from './integration-description.component';
import { IntegrationHistoryComponent } from './integration-history.component';
import { IntegrationMetricsComponent } from './integration-metrics.component';

export const INTEGRATION_DETAIL_DIRECTIVES: Type<any>[] = [
  IntegrationDescriptionComponent,
  IntegrationHistoryComponent,
  IntegrationMetricsComponent
];
