import {
  Component
} from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'syndesis-integration-import-export',
  templateUrl: 'integration-import-export.component.html'
})
export class IntegrationImportExportComponent {

  constructor(
    private router: Router
  ) {}

  redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
