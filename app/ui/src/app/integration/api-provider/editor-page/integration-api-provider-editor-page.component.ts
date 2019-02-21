import { Component } from '@angular/core';
import { IntegrationEditPage } from '../../edit-page/edit-page.component';
import { CanComponentDeactivate } from '@syndesis/ui/platform';
import { RouterStateSnapshot } from '@angular/router';

@Component({
  selector: 'syndesis-integration-api-provider-operation-editor-page',
  templateUrl: './integration-api-provider-editor-page.component.html',
  styleUrls: ['integration-api-provider-editor-page.component.scss'],
})
export class ApiProviderOperationsEditorComponent extends IntegrationEditPage
  implements CanComponentDeactivate {
  canDeactivate(nextState: RouterStateSnapshot) {
    return (
      nextState.url.endsWith('edit') ||
      nextState.url.endsWith('operations') ||
      super.canDeactivate(nextState)
    );
  }
}
