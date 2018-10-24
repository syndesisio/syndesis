import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import { Connections, Connection } from '@syndesis/ui/platform';
import { ConnectionStore } from '@syndesis/ui/store/connection/connection.store';

@Component({
  selector: 'syndesis-connections-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss']
})
export class ConnectionsListPage implements OnInit {
  loading: Observable<boolean>;
  connections: Observable<Connections>;
  filteredConnections: Subject<Connections> = new BehaviorSubject(
    <Connections>{}
  );

  constructor(private store: ConnectionStore, private router: Router) {
    this.loading = store.loading;
    this.connections = store.listVisible;
  }

  ngOnInit() {
    this.store.loadAll();
  }

  onSelected(connection: Connection) {
    this.store.clear();
    this.router.navigate(['connections', connection.id]);
  }
}
