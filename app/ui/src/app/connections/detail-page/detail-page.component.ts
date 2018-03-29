import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Connection, MessageCode, MessageLevel } from '@syndesis/ui/platform';
import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'syndesis-connection-detail-page',
  template: `
    <syndesis-connection-detail-breadcrumb></syndesis-connection-detail-breadcrumb>
    <syndesis-loading [loading]="loading | async">
      <ng-container *ngIf="connection">
        <syndesis-connection-detail-info [connection]="connection"
                                         (updated)="update($event)">
        </syndesis-connection-detail-info>
        <br *ngIf="connection.board?.messages?.length !== 0">
        <syndesis-connection-detail-configuration [connection]="connection"
                                                  (updated)="update($event)">
        </syndesis-connection-detail-configuration>
      </ng-container>
    </syndesis-loading>
  `,
  styles: [
    `
  :host {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: visible;
  }

  syndesis-connection-detail-breadcrumb {
    flex: 0 0 auto;
  }

  syndesis-loading {
    flex: 1 1 auto;
    overflow-y: scroll;
    overflow-x: hidden;
    margin-right: -20px;
    padding-right: 20px;
    padding-bottom: 20px;
  }

`
  ]
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
