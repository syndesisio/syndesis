import { Component, Input } from '@angular/core';

import { OnInit } from '@angular/core/src/metadata/lifecycle_hooks';
import { Observable } from 'rxjs/Observable';
import { Exchange } from '@syndesis/ui/model';
import { Integration } from '../integration.model';
import { IntegrationSupportService } from '../integration-support.service';

@Component({
  selector: 'syndesis-integration-logs',
  template: `
      <button (click)='refresh()'>Refresh</button>
      <table class="table table-striped table-bordered table-hover">
        <thead>
          <tr>
            <th width="150px">Time</th>
            <th >Version</th>
            <th width="150px">Failed</th>
            <th>Steps</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let exchange of exchanges|async|slice:0:100">
            <td>{{ exchange.at | date:'MM/dd/yy hh:mm:ss a' }}</td>
            <td>{{ exchange.ver }}</td>
            <td>{{ exchange.failed }}</td>
            <td>

              <table class="table table-striped table-bordered table-hover">
                <tr>
                  <th>Step</th>
                  <th>At</th>
                  <th>Duration (ms)</th>
                  <th>Status</th>
                </tr>
                <tbody *ngFor="let step of exchange.steps">
                  <tr>
                    <td>{{ step.id }}</td>
                    <td>{{ step.at | date:'MM/dd/yy hh:mm:ss a' }}</td>
                    <td>{{ (step.duration / 1000000).toFixed(3) }}</td>
                    <td *ngIf="step.failure==null">Success</td>
                    <td *ngIf="step.failure!=null">Failed</td>
                  </tr>
                  <tr *ngIf="step.messages && step.messages.length > 0">
                    <td>
                      <pre>
                        <div *ngFor="let message of step.messages">{{ message }}</div>
                      </pre>
                    </td>
                  </tr>
                  <tr *ngIf="step.failure">
                    <td>
                    <pre>{{ step.failure }}</pre>
                    </td>
                  </tr>
                </tbody>
              </table>

            </td>
          </tr>
        </tbody>
      </table>
  `,
})

export class IntegrationLogsComponent implements OnInit {
  @Input() integration: Integration;
  public exchanges: Observable<Exchange[]>;

  constructor(private integrationSupportService: IntegrationSupportService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.exchanges = this.integrationSupportService
      .requestIntegrationLogs(this.integration.id);
  }
}
