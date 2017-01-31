import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ConnectionStore } from '../../../store/connection/connection.store';
import { Connections } from '../../../store/connection/connection.model';

@Component({
  moduleId: module.id,
  selector: 'ipaas-integrations-select-connection',
  templateUrl: 'select-connection.component.html'
})
export class IntegrationsSelectConnectionComponent implements OnInit {

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