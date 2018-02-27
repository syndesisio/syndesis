import { Observable } from 'rxjs/Observable';
import { Component, OnInit, Input } from '@angular/core';

import { Integration, IntegrationSupportService, Activity } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-activity',
  templateUrl: './integration-activity.component.html',
  styleUrls: ['./integration-activity.component.scss']
})
export class IntegrationActivityComponent implements OnInit {
  @Input() integration: Integration;
  activities$: Observable<Activity[]>;

  constructor(private integrationSupportService: IntegrationSupportService) { }

  ngOnInit() {
    this.activities$ = this.integrationSupportService.requestIntegrationActivity(this.integration.id);
  }
}
