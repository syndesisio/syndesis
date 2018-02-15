import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ListConfig, ActionConfig } from 'patternfly-ng';

import { Integration, IntegrationDeployment } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-history',
  templateUrl: './integration-history.component.html',
  styleUrls: ['./integration-history.component.scss']
})
export class IntegrationHistoryComponent {
  @Input() integration: Integration;
  @Input() deploymentActionConfigs: { [id: string]: ActionConfig } = {};
  @Output() deploymentAction = new EventEmitter<{ id: string; deployment: IntegrationDeployment }>();

  deploymentListConfig: ListConfig = {
    selectItems: false,
    showCheckbox: false,
    useExpandItems: true
  };

  usesMapping: { [valueComparator: string]: string } = {
    '=0': '0 Uses',
    '=1': '1 Use',
    'other': '# Uses'
  };

  onDeploymentAction(event: { id: string }, deployment: IntegrationDeployment): void {
    this.deploymentAction.emit({ id: event.id, deployment });
  }
}
