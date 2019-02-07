import { Component, Input } from '@angular/core';
import { FlowToolbarComponent } from '../../edit-page';
import { Flow } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-api-provider-operation-editor-toolbar',
  templateUrl: 'integration-api-provider-editor-toolbar.component.html',
  styleUrls: ['./integration-api-provider-editor-toolbar.component.scss'],
})
export class ApiProviderOperationsToolbarComponent extends FlowToolbarComponent {
  @Input()
  showOperationsButton = false;

  @Input()
  flows: Flow[] = [];

  @Input()
  currentFlow: Flow = undefined;
}
