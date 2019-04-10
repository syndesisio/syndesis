import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription } from 'rxjs';

import { Connection } from '@syndesis/ui/platform';
import { ConnectionStore } from '@syndesis/ui/store/connection/connection.store';

@Component({
  selector: 'syndesis-connection-detail-page',
  template: `
    <syndesis-connection-detail-breadcrumb></syndesis-connection-detail-breadcrumb>
    <syndesis-loading [loading]="loading | async" class="syn-scrollable--body" [content]="content">
      <ng-template #content>
        <ng-container *ngIf="connection">
          <syndesis-connection-detail-info [connection]="connection"
                                          (updated)="update($event)">
          </syndesis-connection-detail-info>
          <br *ngIf="connection.board?.messages?.length !== 0">
          <syndesis-connection-detail-configuration [connection]="connection"
                                                    (updated)="update($event)">
          </syndesis-connection-detail-configuration>
        </ng-container>
      </ng-template>
    </syndesis-loading>
  `,
  styleUrls: ['./detail-page.component.scss']
})
export class ConnectionDetailPageComponent implements OnInit, OnDestroy {
  connection: Connection;
  loading: Observable<boolean>;

  private subscription: Subscription;

  constructor(
    private connectionStore: ConnectionStore,
    private route: ActivatedRoute
  ) {
    this.loading = connectionStore.loading;
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      this.subscription = this.connectionStore
        .load(id)
        .subscribe(connection => {
          this.connection = connection;
        });
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  update(connection) {
    this.connection = connection;
    this.connectionStore.update(connection);
  }
}
