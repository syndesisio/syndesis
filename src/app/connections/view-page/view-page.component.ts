import { Component, OnDestroy } from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute } from '@angular/router';
import '@ngrx/core/add/operator/select';

import * as fromRoot from '../../store/store';
import { SelectAction } from '../../store/connection/connection.actions';

@Component({
  selector: 'ipaas-connection-view-page',
  templateUrl: './view-page.component.html',
  styleUrls: ['./view-page.component.scss'],
})
export class ConnectionViewPage implements OnDestroy {
  actionsSubscription: Subscription;

  constructor(store: Store<fromRoot.State>, route: ActivatedRoute) {
    this.actionsSubscription = route.params
      .select<string>('id')
      .map(id => new SelectAction(id))
      .subscribe(store);
  }

  ngOnDestroy() {
    this.actionsSubscription.unsubscribe();
  }

}
