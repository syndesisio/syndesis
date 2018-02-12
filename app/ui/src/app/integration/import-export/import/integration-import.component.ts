import { Component, Input } from '@angular/core';
import { IntegrationImportState } from '@syndesis/ui/integration/import-export/import';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html'
})
export class IntegrationImportComponent {
  @Input() integrationImportState: IntegrationImportState;

  onSelectedFile(event): void {
    if (event.target && event.target.files) {
      return;
    }
  }

  onSubmit({ valid }, attachFile: boolean): void {
    return;
  }
}
