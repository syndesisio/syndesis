import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';

import { State, getIntegrations } from '../../store/store';
import { Integrations } from '../../store/integration/integration.model';

@Component({
  selector: 'ipaas-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class IntegrationsListPage implements OnInit {

  integrations: Observable<Integrations>;

  constructor(private store: Store<State>) { }

  ngOnInit() {
    this.integrations = this.store.select(getIntegrations);
  }

}
