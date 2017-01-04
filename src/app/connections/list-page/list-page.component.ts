import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import { State, getConnections } from '../../store/store';
import { Connections } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connections-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class ConnectionsListPage implements OnInit {

  connections: Observable<Connections>;

  constructor(private store: Store<State>) { }

  ngOnInit() {
    this.connections = this.store.select(getConnections);
  }

}
