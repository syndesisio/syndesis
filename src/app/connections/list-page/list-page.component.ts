import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ConnectionStore } from '../../store/connection/connection.store';
import { Connections } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connections-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class ConnectionsListPage implements OnInit {

  connections: Observable<Connections>;

  loading: Observable<boolean>;

  constructor(private store: ConnectionStore) {
    this.loading = store.loading;
    this.connections = store.list;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
