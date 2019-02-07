import { Component } from '@angular/core';
import { IntegrationEditPage } from '../../edit-page/edit-page.component';

@Component({
  selector: 'syndesis-integration-api-provider-operation-editor-page',
  templateUrl: './integration-api-provider-editor-page.component.html',
  styleUrls: ['integration-api-provider-editor-page.component.scss']
})
export class ApiProviderOperationsEditorComponent extends IntegrationEditPage {}
