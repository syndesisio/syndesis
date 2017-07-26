import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Connection } from '../../model';
import { ConnectionStore } from '../../store/connection/connection.store';
import { CurrentConnectionService } from '../create-page/current-connection';

@Component({
  selector: 'syndesis-connection-view-wrapper',
  templateUrl: './view-wrapper.component.html',
  styleUrls: ['./view-wrapper.component.scss'],
})
export class ConnectionViewWrapperComponent implements OnInit, OnDestroy {
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
    this.sub = this.route.queryParams.subscribe(params => {
      if (params.edit) {
        this.mode = 'edit';
      }
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }
}
