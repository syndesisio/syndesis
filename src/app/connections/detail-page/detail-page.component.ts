import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Connection } from '../../model';
import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'syndesis-connection-detail-page',
  template: `
    <syndesis-loading [loading]="loading | async">
      <ng-container *ngIf="connection">
        <syndesis-connection-detail-breadcrumb></syndesis-connection-detail-breadcrumb>
        <syndesis-connection-detail-info [connection]="connection"
                                         (updated)="update($event)">
        </syndesis-connection-detail-info>
        <syndesis-connection-detail-configuration [connection]="connection"
                                                  (updated)="update($event)">
        </syndesis-connection-detail-configuration>
      </ng-container>
    </syndesis-loading>
  `,
})
export class ConnectionDetailPageComponent implements OnInit, OnDestroy {

  private subscription: Subscription;
  connection: Connection;
  loading: Observable<boolean>;

  constructor(
    private connectionStore: ConnectionStore,
    private route: ActivatedRoute,
  ) {
    this.loading = connectionStore.loading;
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      this.subscription = this.connectionStore.load(id).subscribe(connection => {
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
