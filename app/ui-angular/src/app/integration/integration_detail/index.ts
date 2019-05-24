import { Type } from '@angular/core';

import { IntegrationActivityComponent } from '@syndesis/ui/integration/integration_detail/integration_activity';
import { IntegrationHistoryComponent } from '@syndesis/ui/integration/integration_detail/integration_history';
import { IntegrationDescriptionComponent } from '@syndesis/ui/integration/integration_detail/integration-description.component';
import { IntegrationMetricsComponent } from '@syndesis/ui/integration/integration_detail/integration-metrics.component';

export * from '@syndesis/ui/integration/integration_detail/integration-detail.component';
export const INTEGRATION_DETAIL_DIRECTIVES: Type<any>[] = [
  IntegrationActivityComponent,
  IntegrationHistoryComponent,
  IntegrationDescriptionComponent,
  IntegrationMetricsComponent
];
