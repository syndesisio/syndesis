import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'ipaas-connection-view-page',
  templateUrl: './view-page.component.html',
})
export class ConnectionViewPage implements OnDestroy {
  private idSubscription: Subscription;

  constructor(store: ConnectionStore, route: ActivatedRoute) {
    this.idSubscription = route.params.pluck<Params, string>('id')
      .map((id) => store.load(id))
      .subscribe();
  }

  ngOnDestroy() { this.idSubscription.unsubscribe(); }
}
