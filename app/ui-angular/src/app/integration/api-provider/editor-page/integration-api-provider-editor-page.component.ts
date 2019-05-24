import { Component } from '@angular/core';
import { CanComponentDeactivate } from '@syndesis/ui/platform';
import { RouterStateSnapshot } from '@angular/router';
import { IntegrationEditPage, INTEGRATION_SAVE } from '../../edit-page';

@Component({
  selector: 'syndesis-integration-api-provider-operation-editor-page',
  templateUrl: './integration-api-provider-editor-page.component.html',
  styleUrls: ['integration-api-provider-editor-page.component.scss'],
})
export class ApiProviderOperationsEditorComponent extends IntegrationEditPage
  implements CanComponentDeactivate {
  canDeactivate(nextState: RouterStateSnapshot) {
    // When switching flows trigger a save operation in the background
    if (
      nextState.url.endsWith('edit') &&
      this.currentFlowService.dirty$.getValue()
    ) {
      this.currentFlowService.events.emit({ kind: INTEGRATION_SAVE });
    }
    return (
      nextState.url.endsWith('edit') ||
      nextState.url.endsWith('operations') ||
      super.canDeactivate(nextState)
    );
  }
}
