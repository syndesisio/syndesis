import { Type } from '@angular/core';

import { IntegrationActivityComponent } from './integration_activity';
import { IntegrationHistoryComponent } from './integration_history';
import { IntegrationDescriptionComponent } from './integration-description.component';
import { IntegrationMetricsComponent } from './integration-metrics.component';

export * from './integration-detail.component';
export const INTEGRATION_DETAIL_DIRECTIVES: Type<any>[] = [
  IntegrationActivityComponent,
  IntegrationHistoryComponent,
  IntegrationDescriptionComponent,
  IntegrationMetricsComponent
];
