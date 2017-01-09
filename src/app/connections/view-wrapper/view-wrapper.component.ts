import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Connection } from '../../store/connection/connection.model';
import { ConnectionStore } from '../../store/connection/connection.store';

@Component({
  selector: 'ipaas-connection-view-wrapper',
  templateUrl: './view-wrapper.component.html',
  styleUrls: ['./view-wrapper.component.scss'],
})
export class ConnectionViewWrapperComponent implements OnInit {
  connection: Observable<Connection>;

  constructor(private store: ConnectionStore) { }

  ngOnInit() { this.connection = this.store.resource; }
}
