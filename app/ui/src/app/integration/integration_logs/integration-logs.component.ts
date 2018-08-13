import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Activity,
  Integration,
  IntegrationSupportService
} from '@syndesis/ui/platform';
import { tap } from 'rxjs/operators';

@Component({
  selector: 'syndesis-integration-logs',
  templateUrl: './integration-logs.component.html'
})
export class IntegrationLogsComponent implements OnInit {
  @Input() integration: Integration;

  enabled$: Observable<boolean>;
  exchanges$: Observable<Activity[]>;

  constructor(private integrationSupportService: IntegrationSupportService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.enabled$ = this.integrationSupportService
      .requestIntegrationActivityFeatureEnabled()
      .pipe(
        tap(isEnabled => {
          if (isEnabled) {
            this.exchanges$ = this.integrationSupportService.requestIntegrationActivity(
              this.integration.id
            );
          }
          return isEnabled;
        })
      );
  }
}
