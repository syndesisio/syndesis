import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import { State, getSelectedConnection } from '../../store/store';

import { Connection } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connection-view-wrapper',
  templateUrl: './view-wrapper.component.html',
  styleUrls: ['./view-wrapper.component.scss'],
})
export class ConnectionViewWrapperComponent implements OnInit {

  connection: Observable<Connection>;

  constructor(private store: Store<State>) { }

  ngOnInit() {
    this.connection = this.store.select(getSelectedConnection);
  }

}
