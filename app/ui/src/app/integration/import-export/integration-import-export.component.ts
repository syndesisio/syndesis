import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'syndesis-integration-import-export',
  templateUrl: 'integration-import-export.component.html'
})
export class IntegrationImportExportComponent {
  constructor(private route: ActivatedRoute, private router: Router) {}
}
