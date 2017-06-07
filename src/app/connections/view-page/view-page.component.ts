import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'syndesis-connection-view-page',
  templateUrl: './view-page.component.html',
})
export class ConnectionViewPage implements OnDestroy, OnInit {
  private idSubscription: Subscription;

  constructor(private store: ConnectionStore,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.idSubscription = this.route.params.pluck<Params, string>('id')
      .map((id) => this.store.load(id))
      .subscribe();
  }

  ngOnDestroy() { this.idSubscription.unsubscribe(); }
}
