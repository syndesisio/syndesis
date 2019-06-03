import { Component, Input } from '@angular/core';
import { FlowToolbarComponent } from '../../edit-page';

@Component({
  selector: 'syndesis-integration-api-provider-operation-editor-toolbar',
  templateUrl: 'integration-api-provider-editor-toolbar.component.html',
  styleUrls: ['./integration-api-provider-editor-toolbar.component.scss'],
})
export class ApiProviderOperationsToolbarComponent extends FlowToolbarComponent {
  @Input()
  showOperationsButton = false;

  getApiOperationFlows() {
    return this.flows.filter(flow => {
      return this.isPrimaryFlow(flow) || this.isApiProviderFlow(flow);
    });
  }

  getApiOperationConditionalFlowGroups() {
    const conditionalFlowGroups = this.getConditionalFlowGroups();
    return conditionalFlowGroups.filter(group => {
      // filter groups with conditional flows that belong to api provider operations other than the current flow
      const primaryFlowId = this.getPrimaryFlowId(group.flows[0]);
      return primaryFlowId === this.currentFlow.id;
    });
  }
}
