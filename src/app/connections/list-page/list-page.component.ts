import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { log, getCategory } from '../../logging';
import { ConnectionStore } from '../../store/connection/connection.store';
import { Connections, Connection } from '../../model';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss']
})
export class ConnectionsListPage implements OnInit {
  loading: Observable<boolean>;
  connections: Observable<Connections>;
  filteredConnections: Subject<
    Connections
  > = new BehaviorSubject(<Connections>{});

  constructor(private store: ConnectionStore, private router: Router) {
    this.loading = store.loading;
    this.connections = store.list;
  }

  ngOnInit() {
    this.store.loadAll();
  }

  onSelected(connection: Connection) {
    this.store.clear();
    this.router.navigate(['connections', connection.id]);
  }
}
