import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { Connection } from '../../model';
import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'syndesis-connection-view-page',
  templateUrl: './view-page.component.html',
})
export class ConnectionViewPage implements OnDestroy, OnInit {

  private idSubscription: Subscription;
  connection: Observable<Connection>;
  public mode = 'view';
  sub: Subscription;
  loading: Observable<boolean>;

  constructor(
    private store: ConnectionStore,
    public route: ActivatedRoute,
    public router: Router,
  ) {
    this.connection = this.store.resource;
    this.loading = store.loading;
  }

  ngOnInit() {
    this.idSubscription = this.route.params
      .pluck<Params, string>('id')
      .map(id => this.store.load(id))
      .subscribe();
    this.sub = this.route.queryParams.subscribe(params => {
      if (params.edit) {
        this.mode = 'edit';
      }
    });
  }

  ngOnDestroy() {
    this.idSubscription.unsubscribe();
    this.sub.unsubscribe();
  }
}
