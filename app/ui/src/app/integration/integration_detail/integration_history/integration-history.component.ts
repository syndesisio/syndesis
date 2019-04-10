import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ListConfig } from 'patternfly-ng';

import { Integration, IntegrationDeployment, IntegrationActionsService } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-history',
  templateUrl: './integration-history.component.html',
  styleUrls: ['./integration-history.component.scss']
})
export class IntegrationHistoryComponent {
  @Input() integration: Integration;
  @Input() deploymentActionConfigs = {};
  @Output()
  deploymentAction = new EventEmitter<{
    id: string;
    deployment: IntegrationDeployment;
  }>();
  @Output() draftAction = new EventEmitter<string>();

  deploymentListConfig: ListConfig = {
    selectItems: false,
    showCheckbox: false,
    useExpandItems: false
  };

  usesMapping: { [valueComparator: string]: string } = {
    '=0': '0 Uses',
    '=1': '1 Use',
    other: '# Uses'
  };

  constructor(public integrationActionsService: IntegrationActionsService) {}

  onDeploymentAction(
    event: { id: string },
    deployment: IntegrationDeployment
  ): void {
    this.deploymentAction.emit({ id: event.id, deployment });
  }

  onDraftAction(eventId: string): void {
    this.draftAction.emit(eventId);
  }
}
